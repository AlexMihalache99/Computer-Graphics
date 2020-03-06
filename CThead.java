import java.io.FileInputStream;
import java.io.FileNotFoundException;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.io.*;

/**
 * CThead.java
 * 
 * @author Alexandru Mihalache 
 * @version 1.0 
 *
 */
public class CThead extends Application {
	private static short cthead[][][]; // store the 3D volume data set
	private static short min, max; // min, max value in the 3D volume data set

	// for resizing the image
	private static WritableImage image;
	private static ImageView imageVIEW;
	
	///for histogram equalization
	private boolean histogramTopOn = false;
	private boolean histogramFrontOn = false;
	private boolean histogramSideOn = false;

	@Override
	public void start(Stage stage) throws FileNotFoundException, IOException {
		stage.setTitle("CThead Viewer");

		ReadData();

		int width = 256;
		int height = 256;

		// Making images for each view: Top, Front, Side.
		WritableImage medical_image = new WritableImage(width, height);
		ImageView imageView = new ImageView(medical_image);

		WritableImage medical_image1 = new WritableImage(width, height);
		ImageView imageView1 = new ImageView(medical_image1);

		WritableImage medical_image2 = new WritableImage(width, height);
		ImageView imageView2 = new ImageView(medical_image2);

		/// Buttons for MIP and resizing
		Button mip_button = new Button("MipTOP");
		Button mip_button1 = new Button("MipFRONT");
		Button mip_button2 = new Button("MipSIDE");
		Button resize_button = new Button("RESIZE");
		Button histogram1 = new Button("Histogram Equalization Top");
		Button histogram2 = new Button("Histogram Equalization Front");
		Button histogram3 = new Button("Histogram Equaliation Side");

		// Sliders for each view
		Slider xslider = new Slider(0, 112, 0);/// for top view
		Slider yslider = new Slider(0, 255, 0);/// for front view
		Slider zslider = new Slider(0, 255, 0);/// for side view

		mip_button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MIP(medical_image, xslider.valueProperty().intValue(), 0, true);
			}
		});

		mip_button1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MIP(medical_image1, yslider.valueProperty().intValue(), 1, true);
			}
		});

		mip_button2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				MIP(medical_image2, zslider.valueProperty().intValue(), 2, true);
			}
		});

		xslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (histogramTopOn == true) {
					histogramEQ(medical_image, newValue.intValue(), 0);
				} else {
					MIP(medical_image, newValue.intValue(), 0, false);
				}

			}
		});

		yslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (histogramFrontOn == true) {
					histogramEQ(medical_image1, newValue.intValue(), 1);
				} else {
					MIP(medical_image1, newValue.intValue(), 1, false);
				}
			}
		});

		zslider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (histogramSideOn == true) {
					histogramEQ(medical_image2, newValue.intValue(), 2);
				} else {
					MIP(medical_image2, newValue.intValue(), 2, false);
				}
			}
		});

		histogram1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (histogramTopOn == false) {
					histogramTopOn = true;
					histogramEQ(medical_image, xslider.valueProperty().intValue(), 0);
				} else {
					histogramTopOn = false;
					MIP(medical_image, xslider.valueProperty().intValue(), 0, false);
				}
			}
		});

		histogram2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (histogramFrontOn == false) {
					histogramFrontOn = true;
					histogramEQ(medical_image1, yslider.valueProperty().intValue(), 1);
				} else {
					histogramFrontOn = false;
					MIP(medical_image1, yslider.valueProperty().intValue(), 1, false);
				}
			}
		});

		histogram3.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (histogramSideOn == false) {
					histogramSideOn = true;
					histogramEQ(medical_image2, zslider.valueProperty().intValue(), 2);
				} else {
					histogramSideOn = false;
					MIP(medical_image2, yslider.valueProperty().intValue(), 2, false);
				}
			}
		});

		FlowPane root = new FlowPane();
		root.setHgap(10);
		root.getChildren().addAll(imageView, mip_button, xslider, histogram1, resize_button);

		FlowPane root1 = new FlowPane();
		root1.setHgap(10);
		root1.getChildren().addAll(imageView1, mip_button1, yslider, histogram2);

		FlowPane root2 = new FlowPane();
		root2.setHgap(10);
		root2.getChildren().addAll(imageView2, mip_button2, zslider, histogram3);

		VBox vbox = new VBox();
		vbox.setSpacing(1);
		vbox.getChildren().addAll(root, root1, root2);

		/// Making a scroll pane which will contain the thumbnails.
		ScrollPane scr = new ScrollPane();
		scr.vbarPolicyProperty().setValue(ScrollPane.ScrollBarPolicy.NEVER);
		scr.setFitToWidth(true);
		scr.setContent(Thumbnails(medical_image));
		vbox.getChildren().add(scr);

		Scene scene = new Scene(vbox, 1000, 1000);
		stage.setScene(scene);
		stage.show();

		scr.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				/// scr.getHvalue() returns how much the user moved
				/// the horizontal bar of the scroll pane.

				MIP(medical_image, rounding(event, 100, Math.floor(scr.getHvalue() * 100) / 100), 0, false);

				HBox hbox = new HBox();
				hbox.getChildren().add(new ImageView(medical_image));
				Scene scene1 = new Scene(hbox, 256, 256);
				stage.setScene(scene1);

				hbox.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent arg0) {
						stage.setScene(scene);
					}

				});
			}
		});

		resize_button.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent arg0) {
				// TODO Auto-generated method stub
				Stage stage1 = new Stage();
				stage.setTitle("Resizing");

				Slider resizeSlider = new Slider(1, 750, 0);

				image = new WritableImage(750, 750);
				imageVIEW = new ImageView(image);

				FlowPane root = new FlowPane();
				root.getChildren().addAll(imageVIEW, resizeSlider);
				root.setAlignment(Pos.CENTER);

				resizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
					public void changed(ObservableValue<? extends Number> observable, Number oldValue,
							Number newValue) {

						image = resizingBI(medical_image, xslider.valueProperty().intValue(),
								resizeSlider.valueProperty().intValue());
						imageVIEW = new ImageView(image);
						root.getChildren().set(0, imageVIEW);

					}
				});

				Scene scene2 = new Scene(root, 1000, 1000);
				stage1.setScene(scene2);
				stage1.show();

			}

		});

	}

	/**
	 * Function to read the cthead data set.
	 * 
	 * @throws IOException
	 */
	public void ReadData() throws IOException {

		File file = new File("CThead.raw");
		DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

		int i, j, k;

		min = Short.MAX_VALUE;
		max = Short.MIN_VALUE;
		short read;
		int b1, b2;

		cthead = new short[113][256][256];

		for (k = 0; k < 113; k++) {
			for (j = 0; j < 256; j++) {
				for (i = 0; i < 256; i++) {

					b1 = ((int) in.readByte()) & 0xff;
					b2 = ((int) in.readByte()) & 0xff;
					read = (short) ((b2 << 8) | b1);
					if (read < min)
						min = read;
					if (read > max)
						max = read;
					cthead[k][j][i] = read;
				}
			}
		}
		in.close();
	}

	/**
	 * This function shows how to carry out an operation on an image. It obtains the
	 * dimensions of the image, and then loops through the image carrying out the
	 * copying of a slice of data into the image.
	 * 
	 * @param image
	 * @param slice
	 * @param view  from which view the image should be displayed. 
	 * 0 - TopDown view
	 * 1 - Front view 
	 * 2 - Side view.
	 * @param isMIP whether we are applying MIP or not for the image.
	 */
	public void MIP(WritableImage image, int slice, int view, boolean isMIP) {
		// Get image dimensions, and declare loop variables
		int w = (int) image.getWidth();
		int h = (int) image.getHeight();
		int i, j, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;

		if (view == 0) {
			if (isMIP == false) {
				for (j = 0; j < h; j++) {
					for (i = 0; i < w; i++) {

						datum = cthead[slice][j][i];

						col = (((float) datum - (float) min) / ((float) (max - min)));
						image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

					} // column loop

				} // row loop
			} else {

				for (j = 0; j < h; j++) {
					for (i = 0; i < w; i++) {

						datum = cthead[slice][j][i];

						int maximum = Short.MIN_VALUE;
						for (k = 0; k < 113; k++) {
							maximum = Math.max(maximum, cthead[k][j][i]);
						}

						col = (((float) maximum - (float) min) / ((float) (max - min)));
						image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

					} // column loop

				} // row loop

			}
		} else if (view == 1) {
			if (isMIP == false) {

				for (k = 0; k < 113; k++) {
					for (i = 0; i < w; i++) {

						datum = cthead[k][slice][i];

						col = (((float) datum - (float) min) / ((float) (max - min)));
						image_writer.setColor(i, k, Color.color(col, col, col, 1.0));

					} /// i - loop

				} /// k -loop
			} else {
				for (k = 0; k < 113; k++) {
					for (i = 0; i < w; i++) {

						datum = cthead[k][slice][i];
						int maximum = Short.MIN_VALUE;
						for (j = 0; j < 256; j++) {
							maximum = Math.max(maximum, cthead[k][j][i]);
						}

						col = (((float) maximum - (float) min) / ((float) (max - min)));
						image_writer.setColor(i, k, Color.color(col, col, col, 1.0));

					} // i -- loop

				} // k -- loop
			}
		} else {
			if (isMIP == false) {

				for (k = 0; k < 113; k++) {
					for (j = 0; j < h; j++) {

						datum = cthead[k][j][slice];

						col = (((float) datum - (float) min) / ((float) (max - min)));
						image_writer.setColor(j, k, Color.color(col, col, col, 1.0));

					} /// j -- loop

				} /// k -- loop

			} else {
				for (k = 0; k < 113; k++) {
					for (j = 0; j < h; j++) {

						datum = cthead[k][j][slice];

						int maximum = Short.MIN_VALUE;
						for (i = 0; i < 256; i++) {
							maximum = Math.max(maximum, cthead[k][j][i]);
						}

						col = (((float) maximum - (float) min) / ((float) (max - min)));
						image_writer.setColor(j, k, Color.color(col, col, col, 1.0));

					} // j -- loop

				} // k -- loop
			}
		}
	}

	/**
	 * This methods is making an Hbox which contains all the slices of an image
	 * which were made smaller.
	 * 
	 * @param img the image from where we want to display all the thumbnails.
	 * @return the Hbox with all the thumbnails.
	 */
	public HBox Thumbnails(WritableImage img) {

		HBox hbox = new HBox();
		ImageView[] images = new ImageView[113];

		for (int i = 0; i < 113; i++) {
			MIP(img, i, 0, false);
			images[i] = new ImageView(resizingNN(img));
			hbox.getChildren().add(images[i]);
		}

		return hbox;
	}

	/**
	 * This method is using the Nearest Neighbour algorithm of scaling an image to
	 * make an image smaller.
	 * 
	 * @param image the image to be resized.
	 * @return the resized image.
	 */
	public WritableImage resizingNN(WritableImage image) {

		int width = (int) image.getWidth();
		int height = (int) image.getHeight();
		float x;
		float y;

		int xa = 100;
		int ya = 100;

		WritableImage image1 = new WritableImage(xa, ya);
		PixelWriter image_writer = image1.getPixelWriter();
		PixelReader pixelReader = image.getPixelReader();

		/// Nearest Neighbour
		for (int j = 0; j < ya; j++) {
			for (int i = 0; i < xa; i++) {

				y = (float) (j * height) / ya;
				x = (float) (i * width) / xa;

				x = Math.round(x);
				y = Math.round(y);
				
				Color color = pixelReader.getColor((int) x, (int) y);
				image_writer.setColor(i, j, color);
			}
		}

		return image1;

	}

	/**
	 * This method is using Billinear Interpolation algorithm of scaling an image to
	 * make it smaller/bigger.
	 * 
	 * @param image the image to be resized.
	 * @param slice which slice of the image should be resized.
	 * @param size  the new size of the image.
	 * @return the resized image.
	 */
	public WritableImage resizingBI(WritableImage image, int slice, int size) {

		float x;
		float y;

		int xb = size;
		int yb = size;

		WritableImage image1 = new WritableImage(750, 750);
		PixelWriter image_writer = image1.getPixelWriter();

		short datum1;
		short datum2;
		short datum3;
		short datum4;

		/// Billinear interpolation
		for (int j = 0; j < yb; j++) {
			for (int i = 0; i < xb; i++) {

				y = (float) j * 255 / yb;
				x = (float) i * 255 / xb;

				int x1 = (int) Math.floor(x);
				int y1 = (int) Math.floor(y);
				int x2 = x1 + 1;
				int y2 = y1 + 1;
				float ratioY = (y - y1) / (y2 - y1);
				float ratioX = (x - x1) / (x2 - x1);

				datum1 = cthead[slice][(int) y1][(int) x1];
				datum2 = cthead[slice][(int) y1][(int) x2];
				datum3 = cthead[slice][(int) y2][(int) x1];
				datum4 = cthead[slice][(int) y2][(int) x2];

				float v1 = (((float) datum1 - (float) min) / ((float) (max - min)));
				float v2 = (((float) datum2 - (float) min) / ((float) (max - min)));
				float v3 = (((float) datum3 - (float) min) / ((float) (max - min)));
				float v4 = (((float) datum4 - (float) min) / ((float) (max - min)));

				float col1 = v1 + (v2 - v1) * ratioX;
				float col2 = v3 + (v4 - v3) * ratioX;
				float col = col1 + (col2 - col1) * ratioY;
				image_writer.setColor(i, j, Color.color(col, col, col, 1.0));

			}
		}
		return image1;

	}

	/**
	 * Method for performing histogram equalization on the data set.
	 * 
	 * @param image The image for a new slice view on which histogram equalization
	 *              is performed.
	 * @param value Variable for the slider to change the slice.
	 */
	public void histogramEQ(WritableImage image, int slice, int view) {
		int w = (int) image.getWidth();
		int h = (int) image.getHeight();
		int i, j, k;
		PixelWriter image_writer = image.getPixelWriter();

		float col;
		short datum;
		int index;
		int histogram[] = new int[max - min + 1];
		float mapping[] = new float[max - min + 1];
		int t[] = new int[max - min + 1];
		float Size = 7405568;

		// initialized the to 0 for all indexes
		for (int p = 0; p < max - min + 1; p++)
			histogram[p] = 0;
		// Stage 1 Creating the histogram
		for (j = 0; j < h; j++) {
			for (i = 0; i < w; i++) {
				for (k = 0; k < 113; k++) {
					index = cthead[k][j][i] - min;
					histogram[index]++;
				}
			}
		}

		// Stage 2 Create the cumulative distribution function t
		for (int n = 0; n < max - min + 1; n++) {
			if (n == 0) {
				t[0] = histogram[0];
			} else {
				t[n] = t[n - 1] + histogram[n];
			}
		}
		// Stage 3 create mapping
		for (int n = 0; n < max - min + 1; n++) {
			mapping[n] = (t[n] / Size);
		}
		// Stage 4 create image
		// histogram equalization for top view
		if (view == 0) {
			for (j = 0; j < h; j++) {
				for (i = 0; i < w; i++) {
					datum = cthead[slice][j][i];
					col = mapping[datum - min];
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
			// histogram equalization for front view
		} else if (view == 1) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < w; i++) {
					datum = cthead[j][slice][i];
					col = mapping[datum - min];
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
			// histogram equalization for side view
		} else if (view == 2) {
			for (j = 0; j < 113; j++) {
				for (i = 0; i < h; i++) {
					datum = cthead[j][i][slice];
					col = mapping[datum - min];
					image_writer.setColor(i, j, Color.color(col, col, col, 1.0));
				}
			}
		} else {
			datum = cthead[0][0][0];
			System.out.println("Histogram error!");
		}
	}

	/**
	 * This method helps in order to know which image the user clicked on from the
	 * scroll pane.
	 * 
	 * @param event        what action the user did on the scroll pane.
	 * @param size         Size of the image.
	 * @param sizeOfHvalue how much the user moved the horizontal bar.
	 * @return the index of the image the user clicked on.
	 */
	public int rounding(MouseEvent event, int size, double sizeOfHvalue) {

		return (int) Math.floor(event.getSceneX() / size + sizeOfHvalue * size);

	}

	public static void main(String[] args) {
		launch();
	}

}