package com.luxlunaris.noadpadlight.control.classes;

import android.util.Log;

import com.luxlunaris.noadpadlight.control.interfaces.NotebookListener;
import com.luxlunaris.noadpadlight.control.interfaces.PageListener;
import com.luxlunaris.noadpadlight.control.interfaces.Pageable;
import com.luxlunaris.noadpadlight.model.classes.BasicBooklet;
import com.luxlunaris.noadpadlight.model.classes.RecycleBin;
import com.luxlunaris.noadpadlight.model.interfaces.Booklet;
import com.luxlunaris.noadpadlight.model.interfaces.Page;

import java.io.File;

/**
 * This is a facade controller that maintains a list of all of the user's pages,
 * and provides methods to create a new page, get a batch of pages of a specified size,
 * and more...
 *
 * It listens to all Pages, and it's listened to by a
 * NotebookListener.
 *
 *
 */
public class Notebook implements Pageable, PageListener {

	/**
	 * The instance of this Singleton class
	 */
	private static Notebook instance;

	/**
	 * Manages the storage of the active existing pages.
	 */
	private static Booklet mainBooklet;

	/**
	 * Manages the recycle bin's dir.
	 */
	private static RecycleBin recycleBin;

	/**
	 * Listens to this Notebook to receive updates on the status
	 * of the Pages therein.
	 */
	private static NotebookListener listener;

	/**
	 * The booklet that is currently set to provide
	 * pages when Notebook's getNext() is called.
	 */
	private static Booklet currentBooklet;


	private Notebook() {
		Log.d("CREATING_NOTEBOOK", this.toString());
		mainBooklet = new BasicBooklet(this,  Paths.PAGES_DIR);
		mainBooklet.load();
		recycleBin = new RecycleBin(Paths.PAGES_RECYCLE_BIN_DIR, this);
		recycleBin.load();
		seePages();
		rewind();
	}

	/**
	 * Notebook is a Singleton
	 * @return
	 */
	public synchronized static Notebook getInstance() {
		return instance!=null? instance : (instance = new Notebook());
	}

	/**
	 * Create and return a new page with a default name: the unix-time of is creation
	 * @return
	 */
	public Page newPage(){
		return newPage(System.currentTimeMillis()+"");
	}

	/**
	 * Create a new page and return it
	 * @param name
	 * @return
	 */
	public Page newPage(String name) {
		Page p = mainBooklet.createPage(name);
		Log.d("BLANK_ON_START", "created page "+ p);
		return p;
	}

	/**
	 * Called by a Page when it gets selected.
	 * Notebook adds it to the list of selected pages
	 * @param page
	 */
	@Override
	public void onSelected(Page page) { }

	/**
	 * Returns an array of the selected pages
	 */
	public Page[] getSelected(){
		return  mainBooklet.getSelected();
	}

	/**
	 * When a page is deleted, it informs the Notebook
	 * @param page
	 */
	@Override
	public void onDeleted(Page page) {

		Log.d("70s", "deleted is empty: " +page.getText().trim().isEmpty());

		//if deleted page is not empty, add it to recycle bin
		if(!page.getText().trim().isEmpty()){
			recycleBin.put(page);
		}


		try {
			listener.onDeleted(page);
		}catch (NullPointerException e){
		}

	}

	@Override
	public void onModified(Page page) {

		try{
			listener.onModified(page);
		}catch (NullPointerException e){
		}
	}

	@Override
	public void onCreated(Page page) {

		Log.d("BLANK_ON_START", "on create "+ page);


		try{
			listener.onCreated(page);
		}catch (NullPointerException e){
			Log.d("BLANK_ON_START", "notebook listener is NULL!!! ");
		}
	}

	/**
	 * Returns the next batch of pages
	 * @param amount
	 * @return
	 */
	@Override
	public Page[] getNext(int amount) {
		return currentBooklet.getNext(amount);
	}

	/**
	 * Get an array of pages by whitespace-separated keywords.
	 * @param query
	 * @return
	 */
	public void searchByKeywords(String query) {
		currentBooklet.searchByKeywords(query);
	}

	/**
	 * Exit search mode.
	 */
	public void exitSearch(){
		currentBooklet.exitSearch();
	}

	/**
	 * Mark all Pages as selected
	 */
	public void selectAll(){
		currentBooklet.selectAll();
	}

	/**
	 * Mark all pages as unselected
	 */
	public void unselectAll(){
		currentBooklet.unselectAll();
	}

	/**
	 * Add a NotebookListener to this Notebook.
	 * @param listener
	 */
	public void setListener(NotebookListener listener){
		this.listener = listener;
	}

	/**
	 * The next batch of pages to deliver is reset to the initial one.
	 */
	public void rewind(){
		currentBooklet.rewind();
	}

	/**
	 * Generate and return a zipped backup file that contains
	 * all of the pages' contents.
	 * @return
	 */
	public File generateBackupFile(){
		return mainBooklet.exportPages();
	}

	/**
	 * Import pages from a zip file.
	 * @param sourcePath
	 */
	public void importPages(String sourcePath){
		mainBooklet.importPages(sourcePath);
	}

	/**
	 * Create a new page that has all of the contents of the selected pages,
	 * and delete the selected pages.
	 */
	public void compactSelection(){
		mainBooklet.compactSelection();
	}


	/**
	 * Restore the selected pages from the recycle bin.
	 */
	public void restoreSelection(){
		for(Page page : recycleBin.getSelected()){
			recycleBin.restore(page);
		}
		recycleBin.unselectAll();
	}

	/**
	 * Delete selected pages.
	 */
	public void deleteSelection(){
		currentBooklet.deleteSelection();
	}

	/**
	 * Permanently delete all of the pages in the recycle bin.
	 * And notify the listening UI that they got deleted.
	 */
	public void emptyRecycleBin(){
		recycleBin.clear();
	}


	/**
	 * Set the  recycle bin as the booklet whose pages
	 * will be sent to the GUI.
	 */
	public void seeRecycleBin(){
		currentBooklet = recycleBin;
		currentBooklet.rewind();
	}

	/**
	 * Set the normal pages booklet as the booklet
	 * whose pages will be sent to the GUI.
	 */
	public void seePages(){
		currentBooklet = mainBooklet;
		currentBooklet.rewind();
	}
















}
