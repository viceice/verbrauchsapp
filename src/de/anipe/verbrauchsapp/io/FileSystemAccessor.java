package de.anipe.verbrauchsapp.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import de.anipe.verbrauchsapp.objects.Brand;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class FileSystemAccessor {

	private static FileSystemAccessor accessor;

	private FileSystemAccessor() {
	}

	public static FileSystemAccessor getInstance() {
		if (accessor == null) {
			accessor = new FileSystemAccessor();
		}
		return accessor;
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)
				|| Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			return true;
		}
		return false;
	}

	public File createOrGetStorageDir(String folderName) {
		// Get the directory for the user's public pictures directory.
//		File file = new File(
//				Environment
//						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), folderName);
		
		File file = new File(
				Environment
						.getExternalStorageDirectory(), folderName);
		
		if (file.exists() && file.isDirectory()) {
			
			
			// XXX
			System.out.println(file.getAbsolutePath());
			
			return file;
		}
		if (!file.mkdirs()) {
			Log.e(FileSystemAccessor.class.getCanonicalName(),
					"Directory not created");
		}
		return file;
	}

	public File getFile() {
		return Environment
				.getExternalStorageDirectory();
	}
	
//	public File getFile() {
//		return Environment
//				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
//	}

	public File[] readFilesFromStorageDir(File folder) {
		if (isExternalStorageReadable()) {
			
			// XXX
			System.out.println(folder.getAbsolutePath());
			
			return folder.listFiles();
		}
		return null;
	}

	public void writeFileToStorageDir(File file, String folderName) {
		// Write the file to given folder
		if (isExternalStorageWritable()) {
			try {
				FileOutputStream out = new FileOutputStream(new File(
						createOrGetStorageDir(folderName), file.getName()));
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public List<String> readCSVFileFromStorage(File csvFile) {
		List<String> res = new LinkedList<String>();

		try {
			BufferedReader in = new BufferedReader(new FileReader(csvFile));
			String zeile = null;
			while ((zeile = in.readLine()) != null) {
				res.add(zeile);
			}

			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res;
	}

	public File writeXMLFileToStorage(Context context, Document doc,
			String folder, String name) throws Exception {
		DOMSource source = new DOMSource(doc);

		System.out.println(doc != null);
		System.out.println(folder != null);
		System.out.println(name != null);
		
		TransformerFactory transformerfactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerfactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH_mm_ss",
				Locale.getDefault());
		String time = dateFormat.format(new Date());

		File resultFile = new File(createOrGetStorageDir(folder), name.replaceAll(" ", "_") + "_" + time + ".xml");
		FileOutputStream stream = new FileOutputStream(resultFile);
		StreamResult result = new StreamResult(stream);
		transformer.transform(source, result);
		
		return resultFile;
	}

	public Bitmap getBitmapForBrand(Context context, Brand value) {
		AssetManager manager = context.getAssets();

		InputStream open = null;
		try {
			open = manager.open(value.toString() + ".png");
			return BitmapFactory.decodeStream(open);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (open != null) {
				try {
					open.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public Bitmap getBitmapForValue(File file) {
		FileInputStream streamIn = null;
		try {
			streamIn = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Bitmap bitmap = BitmapFactory.decodeStream(streamIn);
		try {
			streamIn.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}
}
