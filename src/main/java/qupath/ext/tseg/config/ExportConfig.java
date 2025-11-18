/*
 * QuPath TSEG Extension for Tumor Area Segmentation
 * Copyright (C) 2025 Arif Enes Aydın
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package qupath.ext.tseg.config;

import qupath.lib.roi.interfaces.ROI;

/**
 * Specification for exporting image tiles.
 */
public record ExportConfig(
        ROI roi,
        double targetMPP,
        double sourceMPP,
        int tileSize,
        double overlapFraction,
        String imageExtension
) {
    public ExportConfig {
        if (roi == null) throw new IllegalArgumentException("ROI must not be null");
        if (targetMPP <= 0 || sourceMPP <= 0)
            throw new IllegalArgumentException("targetMPP and sourceMPP must be > 0");
        if (tileSize <= 0) throw new IllegalArgumentException("Tile size > 0");
        if (overlapFraction < 0 || overlapFraction >= 1)
            throw new IllegalArgumentException("Overlap fraction [0,1)");
        if (imageExtension == null || !imageExtension.startsWith("."))
            throw new IllegalArgumentException("Image extension must start with '.'");
    }

    /**
     * Calculates the downsample factor.
     */
    public double downsample() {
        return targetMPP / sourceMPP;
    }

    /**
     * Calculates the overlap in pixels.
     */
    public int overlapPixels() {
        return (int) Math.round(tileSize * overlapFraction);
    }
}
