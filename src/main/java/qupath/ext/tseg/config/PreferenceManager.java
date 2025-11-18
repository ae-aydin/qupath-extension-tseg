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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;
import qupath.fx.prefs.controlsfx.PropertyItemBuilder;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.prefs.PathPrefs;

import java.util.List;
import java.util.ResourceBundle;

/**
 * Manages the user-editable preferences for the TSEG extension.
 * This class creates the JavaFX properties and adds them to the QuPath preferences pane.
 */
public class PreferenceManager {

    public static final IntegerProperty TILE_SIZE = PathPrefs.createPersistentPreference(
            "tileSize", InferenceConfig.DEFAULT.tileSize());
    public static final DoubleProperty TILE_TARGET_MPP = PathPrefs.createPersistentPreference(
            "tileTargetMPP", InferenceConfig.DEFAULT.tileTargetMPP());
    public static final DoubleProperty TILE_OVERLAP = PathPrefs.createPersistentPreference(
            "tileOverlap", InferenceConfig.DEFAULT.tileOverlap());
    public static final StringProperty TILE_IMAGE_FORMAT = PathPrefs.createPersistentPreference(
            "tileImageFormat", InferenceConfig.DEFAULT.tileImageFormat());
    public static final StringProperty DEFAULT_MODEL = PathPrefs.createPersistentPreference(
            "defaultModel", "");
    public static final DoubleProperty CONFIDENCE = PathPrefs.createPersistentPreference(
            "confidence", InferenceConfig.DEFAULT.inferenceConfidence());
    private static final ResourceBundle PREFERENCES_BUNDLE =
            ResourceBundle.getBundle("qupath.ext.tseg.preference");
    private static final List<PrefMeta> PREFERENCES = List.of(
            new PrefMeta(TILE_SIZE, Integer.class, "label.tileSize", "desc.tileSize"),
            new PrefMeta(TILE_TARGET_MPP, Double.class, "label.tileTargetMPP", "desc.tileTargetMPP"),
            new PrefMeta(TILE_OVERLAP, Double.class, "label.tileOverlap", "desc.tileOverlap"),
            new PrefMeta(TILE_IMAGE_FORMAT, String.class, "label.tileExtension", "desc.tileExtension"),
            new PrefMeta(DEFAULT_MODEL, String.class, "label.defaultModel", "desc.defaultModel"),
            new PrefMeta(CONFIDENCE, Double.class, "label.confidence", "desc.confidence")
    );

    /**
     * Adds a preference to the QuPath GUI pane.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addPreference(
            QuPathGUI qupath,
            PrefMeta meta,
            String extensionName
    ) {
        String label = PREFERENCES_BUNDLE.getString(meta.labelKey());
        String description = PREFERENCES_BUNDLE.getString(meta.descKey());

        var propertyItem = new PropertyItemBuilder(meta.property(), meta.type())
                .name(label)
                .category(extensionName)
                .description(description)
                .build();
        qupath.getPreferencePane()
                .getPropertySheet()
                .getItems()
                .add(propertyItem);
    }

    /**
     * Adds all preferences to the pane.
     */
    public static void addPreferencesToPane(QuPathGUI qupath, String extensionName) {
        for (PrefMeta meta : PREFERENCES) {
            addPreference(qupath, meta, extensionName);
        }
    }

    /**
     * Metadata for a preference item.
     */
    private record PrefMeta(
            Property<?> property,
            Class<?> type,
            String labelKey,
            String descKey
    ) {
    }
}