package com.remmi.app.core.android.files

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

    /**                                 Delete
     * Delete a file or directory.
     * @param path The path to the file or directory.
     */
    fun delete(path: String): Boolean

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

    /**
     * List files in a directory.
     * @param directory The directory path relative to filesDir.
     * @return List of filenames.
     */
    fun listFiles(directory: String): List<String>

    /**
     * Ensure a directory exists.
     * @param directory The directory path relative to filesDir.
     */
    fun ensureDirectory(directory: String)
}
