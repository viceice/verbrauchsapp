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
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

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
        return Environment.MEDIA_MOUNTED.equals(state);
    }

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
		String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state)
            || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }

	public File createOrGetStorageDir(String folderName) {
		File file = new File(Environment.getExternalStorageDirectory(),
				folderName);

		if (file.exists() && file.isDirectory()) {
			return file;
		}
		if (!file.mkdirs()) {
			Log.e(FileSystemAccessor.class.getCanonicalName(),
					"Directory not created");
		}
		return file;
	}

	public File getFile() {
		return Environment.getExternalStorageDirectory();
	}

	public File[] readFilesFromStorageDir(File folder) {
		if (isExternalStorageReadable()) {
			File[] files = folder.listFiles();
			
			String[] temp = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				temp[i] = files[i].getAbsolutePath();
			}
			Arrays.sort(temp);
			File[] out = new File[files.length];
			for (int i = 0; i < files.length; i++) {
				out[i] = new File(temp[i]);
			}
			return out;
			
//			return folder.listFiles();
		}
		return null;
	}

	public void writeFileToStorageDir(File file, String folderName) {
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
		List<String> res = new LinkedList<>();

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

		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy.MM.dd-HH.mm.ss", Locale.getDefault());
		String time = dateFormat.format(new Date());

		File resultFile = new File(createOrGetStorageDir(folder),
				name.replaceAll(" ", "_") + "_" + time + ".xml");
		FileOutputStream stream = new FileOutputStream(resultFile);

		xmlOutput.output(doc, stream);

		return resultFile;
	}

	public Document readXMLDocumentFromFile(String folder, String name)
			throws Exception {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(new File(folder, name));
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
