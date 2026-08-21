package com.remmi.app.core.file

/**
 * FILE SERVICE
 *
 * Interface for standard file operations.
 */
interface FileService {

    // ----------------------------------------------------------------------------
    //                             INTERFACE FUNCTIONS
    // ----------------------------------------------------------------------------

    /**                                 Read Text
     * Read content from a file as a string.
     * @param fileName The name of the file to read.
     * @param useAssets If true, read from the Android assets folder.
     * */
    fun readText(fileName: String, useAssets: Boolean = false): String

    /**                                 Write Text
     * Write content to a file.
     * @param fileName The name of the file to write to.
     * @param content The string content to be written.
     * */
    fun writeText(fileName: String, content: String)

    /**                                 Exists
     * Check if a file exists.
     * @param fileName The name of the file to check.
     * */
    fun exists(fileName: String): Boolean

    /**
     * Save an image to a specific directory.
     * @param bytes The image data.
     * @param folder The folder name (e.g., "DCIM/Remmi/RecipeBook").
     * @param fileName The name of the image file.
     * @return The absolute path of the saved image.
     */
    fun saveImage(bytes: ByteArray, folder: String, fileName: String): String?
}
