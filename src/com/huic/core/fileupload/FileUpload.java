package com.huic.core.fileupload;

public class FileUpload extends FileUploadBase {

	// ----------------------------------------------------------- Data members

	/**
	 * The factory to use to create new form items.
	 */
	private FileItemFactory fileItemFactory;

	// ----------------------------------------------------------- Constructors

	/**
	 * Constructs an instance of this class which uses the default factory to
	 * create <code>FileItem</code> instances.
	 *
	 * @see #FileUpload(FileItemFactory)
	 */
	public FileUpload() {
		super();
	}

	/**
	 * Constructs an instance of this class which uses the supplied factory to
	 * create <code>FileItem</code> instances.
	 *
	 * @see #FileUpload()
	 */
	public FileUpload(FileItemFactory fileItemFactory) {
		super();
		this.fileItemFactory = fileItemFactory;
	}

	// ----------------------------------------------------- Property accessors

	/**
	 * Returns the factory class used when creating file items.
	 *
	 * @return The factory class for new file items.
	 */
	public FileItemFactory getFileItemFactory() {
		return fileItemFactory;
	}

	/**
	 * Sets the factory class to use when creating file items.
	 *
	 * @param factory The factory class for new file items.
	 */
	public void setFileItemFactory(FileItemFactory factory) {
		this.fileItemFactory = factory;
	}

}
