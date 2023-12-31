package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.google.gson.Gson;

import java.net.http.HttpRequest.BodyPublishers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Controller {
	@FXML
	private Label urlMessageLabel; // Label that shows error message in audio URL tab.
	@FXML
	private Label fileMessageLabel; // Label that shows error message in File Upload tab.
	@FXML
	private TextField urlField; // User can enter Audio URL here.
	@FXML
	private Button makeSubsFromURL; // Starts the subtitle making process in audio URL tab.
	@FXML
	private Button makeSubsFromFile; // Starts the subtitle making process in file upload tab.
	@FXML
	private ProgressIndicator urlIndicator; // Shows the process of making subs using URL.

	private final String apiUrl = "https://api.assemblyai.com/v2/"; // API URL
	private final String transcriptString = "transcript", uploadString = "upload"; // ENDPOINTS to tell if we are
																					// uploading a file or making subs.

	private final String apiKey = "c7dc6b7ef5c64db2bde2237bc3761dc8"; // API key
	private String audioUrl = "https://bit.ly/3yxKEIY"; // default audio link to be used.

	private ReturnedText result;
	private Stage stage;

	public void initialize(Stage stage) {
		this.stage = stage;

		SetChangeListenerInTextField();
	}

	private void SetChangeListenerInTextField() {
		urlField.textProperty().addListener((x, y, z) -> {
			String inputText = urlField.getText();
			int n = inputText.length();

			if (n <= 5) {
				makeSubsFromURL.setDisable(true);
				return;
			}

			audioUrl = urlField.getText().trim();
			makeSubsFromURL.setDisable(false);
		});
	}

	private File SelectFile() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select File");

		File selectedFile = fileChooser.showOpenDialog(stage);

		if (selectedFile == null)
			return null;

		selectedFile.setReadable(true);
		selectedFile.setWritable(true);

		System.out.println("File Selected");
		return selectedFile;
	}

	private UploadURL CreateFileUploadLink() {
		File selectedFile = SelectFile();
		if (selectedFile == null) {
			fileMessageLabel.setText("No File Selected");
			return null;
		}

		try {
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl + uploadString))
					.header("Authorization", apiKey).header("Transer-Encoding", "chunked")
					.POST(BodyPublishers.ofFile(selectedFile.toPath())).build();

			System.out.println("Request Made");

			HttpClient client = HttpClient.newBuilder().build();
			System.out.println("Request Sent");

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			System.out.println("Response Recieved");

			Gson gson = new Gson();
			UploadURL uploadURL = new UploadURL();
			uploadURL = gson.fromJson(response.body(), UploadURL.class);

			System.out.println("URL made");
			return uploadURL;

		} catch (FileNotFoundException e) {
			fileMessageLabel.setText(e.getMessage());
		} catch (IOException e) {
			fileMessageLabel.setText(e.getMessage());
		} catch (InterruptedException e) {
			fileMessageLabel.setText(e.getMessage());
		} catch (Exception e) {
			fileMessageLabel.setText(e.getMessage());
		}

		return null;
	}

	
	private void Fetch(String url, Label messageLabel) {
		Gson gson = new Gson();

		result = new ReturnedText();
		result.setAudio_url(url);

		String jsonPostString = gson.toJson(result);
		
		
		

		try {
			HttpRequest request = HttpRequest.newBuilder().uri(new URI(apiUrl + transcriptString))
					.header("Authorization", apiKey).POST(BodyPublishers.ofString(jsonPostString)).build();

			HttpClient client = HttpClient.newBuilder().build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() != 200) {
				System.out.println(response.body());
				return;
			}

			result = gson.fromJson(response.body(), ReturnedText.class);

			request = HttpRequest.newBuilder().uri(new URI(apiUrl + transcriptString + "/" + result.getId()))
					.header("Authorization", apiKey).GET().build();

			while (!result.getStatus().equals("completed")) {
				Thread.sleep(2500);

				response = client.send(request, HttpResponse.BodyHandlers.ofString());

				if (response.statusCode() != 200) {
					System.out.println(response.body());
					return;
				}

				result = gson.fromJson(response.body(), ReturnedText.class);

				if (result.getStatus().equals("error")) {
					messageLabel.setText(result.getError());
					break;
				}
			}

			if (result.getStatus() == "error") {
				messageLabel.setText(result.getError());
			}

		} catch (URISyntaxException e) {
			messageLabel.setText(e.getMessage());
		} catch (InterruptedException e) {
			messageLabel.setText(e.getMessage());
		} catch (IOException e) {
			messageLabel.setText(e.getMessage());
		} catch (Exception e) {
			messageLabel.setText(e.getMessage());
		}
	}

	public void MakeSubtitlesUsingURL() {
		MakeSubtitles(audioUrl, urlMessageLabel);
		urlIndicator.setVisible(false);
	}

	public void MakeSubtitlesUsingFile() {
		UploadURL uploadURL = CreateFileUploadLink();
		if (uploadURL == null) {
			fileMessageLabel.setText("File Upload Failed");
			return;
		}

		MakeSubtitles(uploadURL.getUploadUrl(), fileMessageLabel);
	}

	private void MakeSubtitles(String url, Label messageLabel) {
		Fetch(url, messageLabel);

		String subFormatString = result.SubtitleFormatString();

//		Make an instance of file file chooser.
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save File");
		SetExtentionFilter(fileChooser, "srt");

//		Open save file dialog.
		File savedfile = fileChooser.showSaveDialog(stage);

		if (savedfile == null) {
			messageLabel.setText("File Not saved");
			return;
		}

//		Change Extention to txt so that we can write in it.
		changeExtension(savedfile, "txt");

		try (PrintWriter pw = new PrintWriter(savedfile)) {
			pw.print(subFormatString);
		} catch (Exception e) {
			messageLabel.setText(e.getMessage());
		}

//		Change the file extention back to srt.
		changeExtension(savedfile, "srt");
	}

	/**
	 * Filters the files with extension provided in File chooser Dialog.
	 * 
	 * @param fileChooser Extension filter will be set in this file choooser.
	 * @param extension   Extension that will be filtered.
	 */
	public void SetExtentionFilter(FileChooser fileChooser, String extension) {
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
				"FILE EXTENTION : (*." + extension + ")", "*." + extension);
		fileChooser.getExtensionFilters().add(extensionFilter);
	}

	public static File changeExtension(File f, String newExtension) {
		int i = f.getName().lastIndexOf('.');
		String name = f.getName().substring(0, i);
		return new File(f.getParent(), name + newExtension);
	}
}
