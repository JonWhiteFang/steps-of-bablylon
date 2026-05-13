#!/usr/bin/env python3
"""
Render the Steps of Babylon Play Store hi-res icon (512×512 PNG).

Re-renders pixel-for-pixel from the same source-of-truth coordinates as the
in-app vector adaptive icon (`app/src/main/res/drawable/ic_launcher_*.xml`):

- 108×108 design viewport, scaled up by SUPERSAMPLE for crisp edges then
  downscaled with LANCZOS to the final 512×512.
- Solid #0E2247 deep-lapis background (matches `ic_launcher_background.xml`).
- 5-tier stepped-ziggurat silhouette as a single polygon (same 20 vertices
  as the foreground XML's pathData, in the same order).
- Vertical 3-stop linear gradient masked by the silhouette: Gold #D4A843 at
  the polygon's top (y=29) → SandStone #C2B280 at the middle (y=54) →
  lightened DeepBronze #8B5A3A at the bottom (y=79).

If you change the icon design, edit BOTH this script AND the matching XML
under `app/src/main/res/drawable/`. Keeping them in sync is a manual
discipline; see CHANGELOG.md "App launcher icon" entry for the design
rationale (color choices, safe-zone geometry, why solid background).

Usage:
    python3 tools/render_play_store_icon.py
    # writes docs/release/store-assets/play-store-icon-512.png

No third-party dependencies beyond Pillow (already on macOS dev box).
"""

from __future__ import annotations

import os
import sys
from pathlib import Path

try:
    from PIL import Image, ImageDraw
except ImportError as e:
    sys.stderr.write(
        "Pillow is required. Install via `python3 -m pip install Pillow` "
        "or your system package manager.\n"
    )
    raise SystemExit(1) from e


# ---------------------------------------------------------------------------
# Design constants — keep in lockstep with
# app/src/main/res/drawable/ic_launcher_background.xml + ic_launcher_foreground.xml
# ---------------------------------------------------------------------------

# Adaptive-icon design viewport (Android standard). Both XMLs use viewportWidth=108,
# viewportHeight=108. We scale up by SUPERSAMPLE before drawing so polygon edges
# anti-alias cleanly when downscaled to OUT_SIZE.
VIEWPORT = 108
OUT_SIZE = 512
SUPERSAMPLE = 4  # render at 2048×2048, downscale to 512×512

# Background color — `ic_launcher_background.xml` android:fillColor="#FF0E2247".
BACKGROUND_RGB = (0x0E, 0x22, 0x47)

# Foreground ziggurat polygon — vertices in viewport coords, traced from the
# foreground XML's pathData clockwise from the bottom-left. The 5 tiers are
# 10dp tall and step in 6dp on each side; tower visual center at (54, 54).
ZIGGURAT_POLYGON = [
    (22, 79), (86, 79),  # bottom edge
    (86, 69), (80, 69),  # tier 1 → 2 step (right)
    (80, 59), (74, 59),  # tier 2 → 3 step
    (74, 49), (68, 49),  # tier 3 → 4 step
    (68, 39), (62, 39),  # tier 4 → 5 step
    (62, 29), (46, 29),  # top of tier 5
    (46, 39), (40, 39),  # tier 5 → 4 step (left)
    (40, 49), (34, 49),  # tier 4 → 3 step
    (34, 59), (28, 59),  # tier 3 → 2 step
    (28, 69), (22, 69),  # tier 2 → 1 step
]

# Vertical 3-stop linear gradient stops — must match the <aapt:attr> gradient
# in `ic_launcher_foreground.xml`. Y coordinates are in viewport space and
# correspond to the ziggurat's top (29), middle (54), and bottom (79).
GRADIENT_STOPS = [
    (29, (0xD4, 0xA8, 0x43)),   # Gold (brand)
    (54, (0xC2, 0xB2, 0x80)),   # SandStone (brand)
    (79, (0x8B, 0x5A, 0x3A)),   # lightened DeepBronze (see CHANGELOG)
]


# ---------------------------------------------------------------------------
# Rendering
# ---------------------------------------------------------------------------

def lerp(a: int, b: int, t: float) -> int:
    """Linear interpolation between two int channel values."""
    return round(a + (b - a) * t)


def gradient_color_at(y: float) -> tuple[int, int, int]:
    """Sample the 3-stop vertical gradient at viewport y coordinate.

    Stops outside [29, 79] clamp to the nearest stop's color (matches Android
    gradient extend behaviour at the silhouette's exact polygon bounds).
    """
    if y <= GRADIENT_STOPS[0][0]:
        return GRADIENT_STOPS[0][1]
    if y >= GRADIENT_STOPS[-1][0]:
        return GRADIENT_STOPS[-1][1]
    for i in range(len(GRADIENT_STOPS) - 1):
        y0, c0 = GRADIENT_STOPS[i]
        y1, c1 = GRADIENT_STOPS[i + 1]
        if y0 <= y <= y1:
            t = (y - y0) / (y1 - y0)
            return (lerp(c0[0], c1[0], t), lerp(c0[1], c1[1], t), lerp(c0[2], c1[2], t))
    # Unreachable given the early-return clamps above.
    return GRADIENT_STOPS[-1][1]


def render_icon(out_path: Path) -> None:
    """Render the icon at SUPERSAMPLE × OUT_SIZE then downscale to OUT_SIZE."""
    big_size = OUT_SIZE * SUPERSAMPLE  # 2048 by default
    scale = big_size / VIEWPORT  # 2048/108 ≈ 18.96

    # 1. Solid background.
    canvas = Image.new("RGB", (big_size, big_size), BACKGROUND_RGB)

    # 2. Vertical gradient image at the supersample resolution. Each pixel row
    #    samples the gradient at its corresponding viewport-y coordinate.
    gradient = Image.new("RGB", (big_size, big_size))
    grad_pixels = gradient.load()
    if grad_pixels is None:
        raise RuntimeError("Pillow returned a None pixel-access object")
    for py in range(big_size):
        # Map pixel-y back to viewport-y; the gradient is constant per row.
        vy = py / scale
        color = gradient_color_at(vy)
        # Set the whole row to this color via paste to avoid Python loop over x.
        # `gradient.paste(color, (0, py, big_size, py + 1))` would also work but
        # iterating once is simpler and big_size pastes is the same cost.
        for px in range(big_size):
            grad_pixels[px, py] = color

    # 3. Polygon mask in 'L' mode — full-alpha inside the silhouette.
    mask = Image.new("L", (big_size, big_size), 0)
    scaled_polygon = [(round(x * scale), round(y * scale)) for (x, y) in ZIGGURAT_POLYGON]
    ImageDraw.Draw(mask).polygon(scaled_polygon, fill=255)

    # 4. Composite gradient onto background through the mask.
    canvas.paste(gradient, (0, 0), mask)

    # 5. Downscale supersample → final 512×512 with high-quality LANCZOS.
    out = canvas.resize((OUT_SIZE, OUT_SIZE), Image.Resampling.LANCZOS)

    # 6. Save with optimization. Play Store accepts 32-bit PNG up to 1024 KB;
    #    this geometry compresses to well under 100 KB.
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out.save(out_path, "PNG", optimize=True)


def main() -> int:
    repo_root = Path(__file__).resolve().parent.parent
    out = repo_root / "docs" / "release" / "store-assets" / "play-store-icon-512.png"
    render_icon(out)
    size_kb = out.stat().st_size / 1024
    print(f"Wrote {out.relative_to(repo_root)} ({size_kb:.1f} KB, {OUT_SIZE}x{OUT_SIZE})")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
