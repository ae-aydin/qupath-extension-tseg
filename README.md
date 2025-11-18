# QuPath Tumor Segmentation Extension

**QuPath Version: 0.6.0**

An extension for QuPath that enables deep learning based tumor segmentation by integrating a Python inference environment. It sends a selected region as tiles for inference and integrates the resulting predictions back into QuPath as GeoJSON annotations.

***

### Requirements

*   An active **internet connection** for the one-time setup.
*   **`curl`** command-line tool.

***

### One-Time Setup

On the first run, the extension automatically sets up its environment. This process runs only once and will:

1.  Create an inference directory in your QuPath user extension folder.
2.  Download the Python inference repository [tseg-qupath-inf](https://github.com/ae-aydin/tseg-qupath-inf).
3.  Set up the Python environment using `uv` (a fast Python installer).
4.  Download the default segmentation models.

***

### Usage

1.  Open an image within a QuPath project and draw an annotation. The image must have a defined Microns per Pixel (MPP) value.
2.  Select the annotation.
3.  Open the extension from the `Extensions` menu.
4.  Adjust the inference settings if needed, then click **Segment Selected Region**.

***

### Project Repositories

*   Model Training: [tseg](https://github.com/ae-aydin/tseg)
*   Inference Script: [tseg-qupath-inf](https://github.com/ae-aydin/tseg-qupath-inf)
*   Extension: [qupath-extension-tseg](https://github.com/ae-aydin/qupath-extension-tseg)

***

### Future Improvements

*   Easy installation feature
*   Catalog for QuPath extension manager
*   More model formats
*   Better models

***

### License

Licensed under the [GPL-3.0](LICENSE).

***

### Attribution

*   <a href="https://www.flaticon.com/free-icons/tumor" title="tumor icons">Tumor icons created by Freepik - Flaticon</a>