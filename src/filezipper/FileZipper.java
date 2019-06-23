package filezipper;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.*;

/**
 * This is a simple program that creates an archive of files and/or directories
 * using a default compression.
 * What I am most proud of in this edition is scaling of the whole window so that
 * it always occupies 1/4 of the screen width and 1/3 of its height approximately.
 * The frame is kept in the same aspect ratio (4:3) regardless of the screen size
 * and resolution. All of the components are also properly scaled (almost all).
 * 
 * The program has also been complimented with many most basic features and
 * functions which were omitted in the lesson. That does not mean it is a complete
 * application similar to commercial products. It is simply a presentation of what
 * I have learned so far and am capable of, what I can create on my own in addition
 * to what has been written in the lesson. For example, one of the key features
 * which has not been implemented is unpacking or unzipping an archive. The reason
 * is simple: I preferred to focus on and devote time to additional things that came
 * to my mind. This program would not have been used for everyday work anyway.
 * It is just a showcasing tool.
 * 
 * @version 1.0
 * 
 * @author Dominik Marcinkowski - update and expansion of the original course
 * lesson Zipper in "Java od Podstaw do Eksperta - twórz własne aplikacje" by
 * Arkadiusz Włodarczyk
 */

@SuppressWarnings("serial")
public class FileZipper extends JFrame
{
	private double screenWidth = this.getGraphicsConfiguration().getDevice().getDisplayMode().getWidth();
	private double screenHeight = this.getGraphicsConfiguration().getDevice().getDisplayMode().getHeight();
	private JButton bAdd;
	private JButton bRemove;
	private JButton bZip;
	private JMenuBar menuBar = new JMenuBar();
	@SuppressWarnings("rawtypes")
	private DefaultListModel listModel = new DefaultListModel()
	{
		ArrayList localList = new ArrayList();
		
		@SuppressWarnings("unchecked")
		@Override
		public void addElement(Object obj)
		{
			localList.add(obj);
			super.addElement( ((File)obj).getName() );
		}
		@Override
		public Object get(int index)
		{
			return localList.get(index);
		}
		@Override
		public Object remove(int index)
		{
			localList.remove(index);			
			return super.remove(index);
		}
	};
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JList list = new JList(listModel);
	private JLabel labelItems = new JLabel("Items:");
	private JLabel labelCounts = new JLabel("0");
	private JFileChooser fileChooser = new JFileChooser();
	private FileNameExtensionFilter extensionFilter = new FileNameExtensionFilter("ZIP archive (*.zip)", "zip");
	private String lastPathUsedForAdding;
	private String lastPathUsedForSaving;
	private String lastSavedName = "Archive.zip";
	
	
	public FileZipper()
	{
		initComponents();
	}
	
	public void initComponents()
	{
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("Zipper");
		
		// Setting the width to 1/4 and height to 1/3 results in the window being actually smaller than those values.
		// To reflect the real size it should take on the screen I increased the size of the frame. Of course in
		// resolutions a lot higher or a lot lower than currently popular (as 1600 x 900 or 1920 x 1080) the window
		// might become a bit smaller than 1/4 of the screen or a bit larger but, in general, it should oscillate
		// around this value. The difference should be insignificant.
		int windowWidth = (int)(screenWidth / 3.86);
		int windowHeight = (int)(screenHeight / 2.92525);
		
		// I came up with a factor which serves as a single value reflecting a perfect 4:3 ratio.
		// If a particular resolution corresponds to a bigger or lower factor it means it is not in the 4:3 aspect ratio.
		// 4:3 ratio has the factor 7 (width + height / width - height).
		// But to really get 1/4 and 1/3 of the screen the factor must be 6.738317757
		// (because the above windowWidth and Height are not exactly 1/4 and 1/3 but more).
		
		//this.setBounds(0, 0, 480, 360);
		Dimension windowDimensions = new Dimension(calculateWindowSize(windowWidth, windowHeight));
		windowWidth = windowDimensions.width;
		windowHeight = windowDimensions.height;
		this.setBounds(0, 0, windowWidth, windowHeight);
		this.setLocationRelativeTo(null);
		
		this.setJMenuBar(menuBar);
		JMenu menuFile = menuBar.add(new JMenu("File"));
		
		int iconSize = (int)(windowWidth * 0.05);
		Image imageAdd = new ImageIcon("Data/Add.png").getImage();
		Image scaledImgAdd = imageAdd.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		Image imageRemove = new ImageIcon("Data/Remove.png").getImage();
		Image scaledImgRem = imageRemove.getScaledInstance(iconSize, iconSize, Image.SCALE_SMOOTH);
		
		// Got rid of the Key Accelerators because of the problems with scaling fonts for them in the File menu.
		Action actionAdd = new MyAction("Add", "Add files or directories to the list of items to be zipped"/*, "ctrl d"*/, new ImageIcon(scaledImgAdd));
		Action actionRemove = new MyAction("Remove", "Remove the currently selected items from the list"/*, "ctrl r"*/, new ImageIcon(scaledImgRem));
		Action actionZip = new MyAction("Zip", "Create an archive"/*, "ctrl z"*/);
		
		Font toolTipFont = new Font(Font.DIALOG, Font.PLAIN, (int)(windowWidth * 0.031));
		
		JMenuItem menuAdd = new MyMenuItem(actionAdd, toolTipFont);
		menuFile.add(menuAdd);
		//JMenuItem menuAdd = menuFile.add(actionAdd);
		JMenuItem menuRemove = new MyMenuItem(actionRemove, toolTipFont);
		menuFile.add(menuRemove);
		//JMenuItem menuRemove = menuFile.add(actionRemove);
		menuFile.addSeparator();
		JMenuItem menuZip = new MyMenuItem(actionZip, toolTipFont);
		menuFile.add(menuZip);
		//JMenuItem menuZip = menuFile.add(actionZip);
		
		bAdd = new MyButton(actionAdd, toolTipFont);
		bRemove = new MyButton(actionRemove, toolTipFont);
		bZip = new MyButton(actionZip, toolTipFont);
		
		// Adding a MenuKeyListener to every focusable object to be able to use Alt to highlight File menu
		// regardless of the currently focused object.
		list.setBorder(BorderFactory.createEtchedBorder());
		list.addKeyListener(new MenuKeyListener());
		JScrollPane scrolledList = new JScrollPane(list);
		
		GroupLayout layout = new GroupLayout(this.getContentPane());
		
		int listMinSize = (int)(windowWidth * 0.405);
		int listPrefSize = (int)(windowWidth * 0.604);
		int listGap = (int)(windowWidth * 0.04);
		int listMinHeight = (int)(windowWidth * 0.244);
		int buttonsPrefSize = (int)(windowWidth * 0.274);
		int buttonsMinSize = (int)(windowWidth * 0.244);
		int buttonZipPrefSize = (int)(windowWidth * 0.162);
		int buttonZipMinSize = (int)(windowWidth * 0.123);
		int buttonsPrefHeight = (int)(windowWidth * 0.082);
		int buttonsMinHeight = (int)(windowWidth * 0.061);
		int gapBtAddRemoveButtons = (int)(windowWidth * 0.013);
		int gapBtRemoveItems = (int)(windowWidth * 0.01);
		int gapBtItemsCounts = (int)(windowWidth * 0.007);
		int gapBtCountsZip = (int)(windowWidth * 0.02);
		int containerGap = (int)(windowWidth * 0.025);
		
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
					.addGap(containerGap, containerGap, containerGap)
					.addComponent(scrolledList, listMinSize, listPrefSize, Short.MAX_VALUE)
					.addGap(1, listGap, Short.MAX_VALUE)
					.addGroup(
						layout.createParallelGroup(GroupLayout.Alignment.CENTER, true)
						.addComponent(bAdd, buttonsMinSize, buttonsPrefSize, buttonsPrefSize)
						.addComponent(bRemove, buttonsMinSize, buttonsPrefSize, buttonsPrefSize)
						.addComponent(labelItems, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(labelCounts, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(bZip, buttonZipMinSize, buttonZipPrefSize, buttonZipPrefSize))
						.addGap(containerGap, containerGap, containerGap)
				);
		layout.setVerticalGroup(
				layout.createParallelGroup()
					.addGroup(layout.createSequentialGroup()
						.addGap(containerGap, containerGap, containerGap)
						.addComponent(scrolledList, listMinHeight, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGap(containerGap, containerGap, containerGap))
					.addGroup(
						layout.createSequentialGroup()
						.addGap(containerGap, containerGap, containerGap)
						.addComponent(bAdd, buttonsMinHeight, buttonsPrefHeight, buttonsPrefHeight)
						.addGap(gapBtAddRemoveButtons, gapBtAddRemoveButtons, gapBtAddRemoveButtons)
						.addComponent(bRemove, buttonsMinHeight, buttonsPrefHeight, buttonsPrefHeight)
						.addGap(gapBtRemoveItems, gapBtRemoveItems, gapBtRemoveItems)
						.addComponent(labelItems, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(gapBtItemsCounts, gapBtItemsCounts, gapBtItemsCounts)
						.addComponent(labelCounts, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(gapBtCountsZip, Short.MAX_VALUE)
						.addComponent(bZip, buttonsMinHeight, buttonsPrefHeight, buttonsPrefHeight)
						.addGap(containerGap, containerGap, containerGap))
				);
		
		this.getContentPane().setLayout(layout);
		
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		lastPathUsedForAdding = fileChooser.getCurrentDirectory().getPath();
		lastPathUsedForSaving = fileChooser.getCurrentDirectory().getPath();
		
		int listFontSize = (int)(windowWidth * 0.028);
		list.setFont(new Font(list.getFont().getFontName(), Font.PLAIN, listFontSize));
		int labelFontSize = (int)(windowWidth * 0.032);
		labelItems.setFont(new Font(labelItems.getFont().getFontName(), Font.PLAIN, labelFontSize));
		labelCounts.setFont(new Font(labelCounts.getFont().getFontName(), Font.PLAIN, labelFontSize));
		
		int bFontSize = (int)(windowWidth * 0.0354);
		int bRemoveFontSize = (int)(windowWidth * 0.031);
		int bIconTextGap = (int)(windowWidth * 0.0146);
		bAdd.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, bFontSize));
		bAdd.setIconTextGap(bIconTextGap);
		bRemove.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, bRemoveFontSize));
		bRemove.setIconTextGap(bIconTextGap);
		bZip.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, bFontSize));
		
		bRemove.setMargin(new Insets(0, 0, 0, 0));
		bAdd.setMargin(new Insets(0, 0, 0, 0));
		bZip.setMargin(new Insets(0, 0, 0, 0));
		
		bAdd.setMnemonic(KeyEvent.VK_D);
		bAdd.addKeyListener(new MenuKeyListener());
		menuAdd.setMnemonic(KeyEvent.VK_D);
		bRemove.setMnemonic(KeyEvent.VK_R);
		bRemove.addKeyListener(new MenuKeyListener());
		menuRemove.setMnemonic(KeyEvent.VK_R);
		bZip.setMnemonic(KeyEvent.VK_Z);
		bZip.addKeyListener(new MenuKeyListener());
		menuZip.setMnemonic(KeyEvent.VK_Z);
		
		bRemove.setEnabled(false);
		menuRemove.setEnabled(false);
		bZip.setEnabled(false);
		menuZip.setEnabled(false);
		
		int menuFontSize = (int)(windowWidth * 0.029);
		int menuItemWidth = (int)(windowWidth * 0.29);
		int menuItemHeight = (int)(windowHeight * 0.1);
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuFile.addKeyListener(new MenuKeyListener());
		menuFile.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, menuFontSize));
		menuAdd.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, menuFontSize));
		menuRemove.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, menuFontSize));
		menuZip.setFont(new Font("Arial Rounded MT Bold", Font.PLAIN, menuFontSize));
		menuAdd.setPreferredSize(new Dimension(menuItemWidth, menuItemHeight));
		menuRemove.setPreferredSize(new Dimension(menuItemWidth, menuItemHeight));
		menuZip.setPreferredSize(new Dimension(menuItemWidth, menuItemHeight));
		menuAdd.setIconTextGap(bIconTextGap);
		menuRemove.setIconTextGap(bIconTextGap);
		
		Font fileChooserFont = new Font(Font.DIALOG, Font.BOLD, labelFontSize);
		int chooserButtonsIconSize = (int)(windowWidth * 0.05);
		
		fileChooser.setPreferredSize(new Dimension( (int)(windowWidth * 1.2), windowHeight));
				// JPanel - current directory drop-down
		JPanel chooserPanelTop = (JPanel)fileChooser.getComponent(0);
				// Unused - JPanel - pane to the right of the FilePane
		// JPanel chooserPanelRight = (JPanel)fileChooser.getComponent(1);
				// Unscaled due to access restrictions in sun.swing.FilePane - the main file list
		// fileChooser.getComponent(2);
				// JPanel - the panel below the list
		JPanel chooserPanelBottom = (JPanel)fileChooser.getComponent(3);
		
				// JLabel - Look In:
		chooserPanelTop.getComponent(1).setFont(fileChooserFont);
				// javax.swing.plaf.metal.MetalFileChooserUI$1 - current dir. drop-down
		chooserPanelTop.getComponent(2).setFont(fileChooserFont);
		chooserPanelTop.getComponent(2).setPreferredSize(new Dimension(chooserPanelTop.getComponent(2).getPreferredSize().width, chooserButtonsIconSize));
		
				// chooserPanelTop component(0) (JPanel with 5 buttons):
				// JButton - Up One Level
		Icon buttonIcon = ((JButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(0) ).getIcon();
		Image buttonIconImage = ((ImageIcon)buttonIcon).getImage();
		Image scaledButtonIconImage = buttonIconImage.getScaledInstance(chooserButtonsIconSize, chooserButtonsIconSize, Image.SCALE_SMOOTH);
		((JButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(0) ).setIcon(new ImageIcon(scaledButtonIconImage));
		
				// JButton - Home
		buttonIcon = ((JButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(2) ).getIcon();
		buttonIconImage = ((ImageIcon)buttonIcon).getImage();
		scaledButtonIconImage = buttonIconImage.getScaledInstance(chooserButtonsIconSize, chooserButtonsIconSize, Image.SCALE_SMOOTH);
		((JButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(2) ).setIcon(new ImageIcon(scaledButtonIconImage));
		
				// JButton - Create New Folder
		buttonIcon = ((JButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(4) ).getIcon();
		buttonIconImage = ((ImageIcon)buttonIcon).getImage();
		scaledButtonIconImage = buttonIconImage.getScaledInstance(chooserButtonsIconSize, chooserButtonsIconSize, Image.SCALE_SMOOTH);
		((JButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(4) ).setIcon(new ImageIcon(scaledButtonIconImage));
		
				// JToggleButton - List
		buttonIcon = ((JToggleButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(6) ).getIcon();
				// Doesn't work with JToggleButton's Icon for some reason
		//buttonIconImage = ((ImageIcon)buttonIcon).getImage();
				// have to use Buffered Image instead
		BufferedImage buttonBufferedImage = new BufferedImage(buttonIcon.getIconWidth(), buttonIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		buttonIcon.paintIcon(null, buttonBufferedImage.getGraphics(), 0, 0);
		Image scaledButtonBufferedImage = buttonBufferedImage.getScaledInstance(chooserButtonsIconSize, chooserButtonsIconSize, Image.SCALE_SMOOTH);
		((JToggleButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(6) ).setIcon(new ImageIcon(scaledButtonBufferedImage));
		
				// JToggleButton - Details
		buttonIcon = ((JToggleButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(7) ).getIcon();
		buttonBufferedImage = new BufferedImage(buttonIcon.getIconWidth(), buttonIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		buttonIcon.paintIcon(null, buttonBufferedImage.getGraphics(), 0, 0);
		scaledButtonBufferedImage = buttonBufferedImage.getScaledInstance(chooserButtonsIconSize, chooserButtonsIconSize, Image.SCALE_SMOOTH);
		((JToggleButton) ((JPanel)chooserPanelTop.getComponent(0)).getComponent(7) ).setIcon(new ImageIcon(scaledButtonBufferedImage));
		
		
		Dimension fileChooserMainButtonsSize = new Dimension((int)(windowWidth * 0.2), (int)(windowWidth * 0.067));
				// first JPanel->Label - File Name:
		((JPanel)chooserPanelBottom.getComponent(0)).getComponent(0).setFont(fileChooserFont);
				// first JPanel->File Chooser for the File Name
		((JPanel)chooserPanelBottom.getComponent(0)).getComponent(1).setFont(fileChooserFont);
				// second JPanel->Label - Files of Type:
		((JPanel)chooserPanelBottom.getComponent(2)).getComponent(0).setFont(fileChooserFont);
				// second JPanel->ComboBox for extensions
		((JPanel)chooserPanelBottom.getComponent(2)).getComponent(1).setFont(fileChooserFont);
				// third JPanel->JButton - Add or Save
		JButton chooserButtonApprove = (JButton) ((JPanel)chooserPanelBottom.getComponent(3)).getComponent(0);
		chooserButtonApprove.setPreferredSize(fileChooserMainButtonsSize);
		chooserButtonApprove.setMargin(new Insets(0, 0, 0, 0));
		chooserButtonApprove.setFont(fileChooserFont);
				// third JPanel->JButton - Cancel
		JButton chooserButtonCancel = (JButton) ((JPanel)chooserPanelBottom.getComponent(3)).getComponent(1);
		chooserButtonCancel.setPreferredSize(fileChooserMainButtonsSize);
		chooserButtonCancel.setMargin(new Insets(0, 0, 0, 0));
		chooserButtonCancel.setFont(fileChooserFont);
		
	}

	public static void main(String[] args)
	{
		new FileZipper().setVisible(true);
	}
	
	/**
	 * As the name implies, this method ensures the main window will run
	 * in 4:3 aspect ratio. If the screen ratio is 16:9 this method will
	 * not do much, only slight correction if any. The below parameters
	 * are already 1/4 and 1/3 of the screen size.
	 * 
	 * @param width potentially 1/4 of the screen width - subject to check
	 * @param height potentially 1/3 of the screen height - subject to check
	 * @return both width and height as a Dimension type already processed to be in 4:3 ratio
	 */
	private Dimension calculateWindowSize(int width, int height)
	{
		final double PERFECT_FACTOR = 6.738317757;
		// You can't divide by zero; this is a square so I imagine the factor would be
		// infinite in this case;
		if (width == height)
			width += 1;
		double currentFactor = ((double)width + (double)height) / ((double)width - (double)height);
		double closestDifference = currentFactor - PERFECT_FACTOR;
		if (closestDifference < 0)
			closestDifference = -closestDifference;
		double currentDifference = closestDifference;
		int bestWidth = width;
		int bestHeight = height;
		@SuppressWarnings("unused")
		Dimension bestDimension;
		
		// A negative factor means the ratio is inverted, so the height is higher than the width.
		if (currentFactor < 0)
		{
			// If the screen is extremely narrow we will extend the window width until the screen
			// size is reached.
			while(width < screenWidth)
			{
				width++;
				if (width == height)
					width += 1;
				currentFactor = ((double)width + (double)height) / ((double)width - (double)height);
				
				// The frame is widened (passing the square) until we reach the correct ratio
				// (width > height, meaning the positive factor).
				if (currentFactor > 0)
				{
					currentDifference = currentFactor - PERFECT_FACTOR;
					
					// Next step is to increase the width until the difference drops below zero.
					// This way we know that we've reached the perfect factor.
					// Width and Height corresponding to the closest difference are memorized
					// and the while loop is broken.
					if (currentDifference <= 0)
					{
						if (currentDifference < 0)
							currentDifference = -currentDifference;
						if (currentDifference < closestDifference)
						{
							closestDifference = currentDifference;
							bestWidth = width;
							bestHeight = height;
						}
						break;
					}
					
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
				}
			}
		}
		// A positive factor but between zero and perfect factor, meaning too flat, too wide window;
		// Factor 0 could be considered as a straight line without the second dimension.
		else if (currentFactor > 0 && currentFactor < PERFECT_FACTOR)
		{
			// Similarly, if the screen is very wide (more likely) we will increase the window height
			// until the screen size is reached.
			while(height < screenHeight)
			{
				// We don't need to check if the values are the same because we will reach the perfect
				// factor before reaching the square shape.
				height++;
				currentFactor = ((double)width + (double)height) / ((double)width - (double)height);
				currentDifference = currentFactor - PERFECT_FACTOR;
				
				if (currentDifference < 0)
				{
					if (currentDifference < 0)
						currentDifference = -currentDifference;
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
				}
				// When the difference reaches or goes beyond zero it means we found our best dimensions.
				else
				{
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
					break;
				}
			}
		}
		// A positive factor but higher than the perfect factor and even the tolerance;
		// It means the frame is closer to being a square than too flat.
		else if (currentFactor > 0 && currentFactor > PERFECT_FACTOR + 0.000000001)
		{
			// We can't reach any of the screen edges this time.
			while(true)
			{
				width++;
				currentFactor = ((double)width + (double)height) / ((double)width - (double)height);
				currentDifference = currentFactor - PERFECT_FACTOR;
				
				if (currentDifference > 0)
				{
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
				}
				// The frame is widened until the perfect factor is reached below.
				else
				{
					if (currentDifference < 0)
						currentDifference = -currentDifference;
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
					break;
				}
			}
		}
		
		// In case we reached the screen width but the window is still too narrow
		// we must decrease the height.
		if (width == screenWidth)
		{
			while(true)
			{
				// It is possible we already reached the perfect factor. In that case we're done.
				if ( !(currentFactor < PERFECT_FACTOR || currentFactor > PERFECT_FACTOR + 0.000000001) )
					break;
				
				height--;
				if (width == height)
					height -= 1;
				
				currentFactor = ((double)width + (double)height) / ((double)width - (double)height);
				
				if (currentFactor > 0)
				{
					currentDifference = currentFactor - PERFECT_FACTOR;
					if (currentDifference <= 0)
					{
						if (currentDifference < 0)
							currentDifference = -currentDifference;
						if (currentDifference < closestDifference)
						{
							closestDifference = currentDifference;
							bestWidth = width;
							bestHeight = height;
						}
						break;
					}
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
				}
			}
		}
		if (height == screenHeight)
		{
			while(true)
			{
				if ( !(currentFactor < PERFECT_FACTOR || currentFactor > PERFECT_FACTOR + 0.000000001) )
					break;
				
				width--;
				currentFactor = ((double)width + (double)height) / ((double)width - (double)height);
				currentDifference = currentFactor - PERFECT_FACTOR;
				
				if (currentDifference < 0)
				{
					if (currentDifference < 0)
						currentDifference = -currentDifference;
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
				}
				else
				{
					if (currentDifference < closestDifference)
					{
						closestDifference = currentDifference;
						bestWidth = width;
						bestHeight = height;
					}
					break;
				}
			}
		}
		
		return bestDimension = new Dimension(bestWidth, bestHeight); 
	}
	
	
	
	private class MyAction extends AbstractAction
	{
		public static final int BUFFER = 1024;
		@SuppressWarnings("rawtypes")
		ArrayList pathList = new ArrayList();
		
		public MyAction(String name, String desc/*, String key*/)
		{
			this.putValue(Action.NAME, name);
			this.putValue(Action.SHORT_DESCRIPTION, desc);
			//this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
		}
		public MyAction(String name, String desc/*, String key*/, Icon icon)
		{
			this(name, desc/*, key*/);
			this.putValue(Action.SMALL_ICON, icon);
		}
		
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			if (arg0.getActionCommand().equals("Add"))
			{
				addItemsToTheList();
				list.requestFocusInWindow();
			}
			else if (arg0.getActionCommand().equals("Remove"))
			{
				removeItemsFromTheList();
				list.requestFocusInWindow();
			}
			else if (arg0.getActionCommand().equals("Zip"))
			{
				createArchive();
				list.requestFocusInWindow();
			}
		}
		
		@SuppressWarnings("unchecked")
		private void addItemsToTheList()
		{
			fileChooser.setDialogTitle("Add to archive");
			fileChooser.setCurrentDirectory(new File(lastPathUsedForAdding));
			fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			fileChooser.setMultiSelectionEnabled(true);
			fileChooser.setAcceptAllFileFilterUsed(true);
			fileChooser.removeChoosableFileFilter(extensionFilter);
			boolean alreadyWarned = false;
			
			int choice = fileChooser.showDialog(rootPane, "Add");
			
			if (choice == JFileChooser.APPROVE_OPTION)
			{				
				File[] selectedFiles = fileChooser.getSelectedFiles();
				String[] currentDirFiles = fileChooser.getCurrentDirectory().list();
				
				for (int i = 0; i < selectedFiles.length; i++)
				{
					if (existsInCurrentDir(currentDirFiles, selectedFiles[i].getName()))
					{
						if (!isAlreadyOnTheList(selectedFiles[i].getPath()))
							listModel.addElement(selectedFiles[i]);
						else if (!alreadyWarned)
						{
							JOptionPane.showMessageDialog(rootPane, "Some of the selected files are already on the list.\nSkipped those.", "Zipper", JOptionPane.WARNING_MESSAGE);
							alreadyWarned = true;
						}
						bRemove.setEnabled(true);
						menuBar.getMenu(0).getMenuComponent(1).setEnabled(true);
						bZip.setEnabled(true);
						menuBar.getMenu(0).getMenuComponent(3).setEnabled(true);
					}
					else
					{
						JOptionPane.showMessageDialog(rootPane, "Unable to find \"" + selectedFiles[i].getName() + "\"", "Zipper", JOptionPane.ERROR_MESSAGE);
						break;
					}
				}
				
				lastPathUsedForAdding = fileChooser.getCurrentDirectory().getPath();
				labelCounts.setText("" + listModel.getSize());
			}
		}
		
		private boolean isAlreadyOnTheList(String newSelectionPath)
		{
			for (int i = 0; i < listModel.getSize(); i++)
			{
				if ( ((File)listModel.get(i)).getPath().equals(newSelectionPath))
					return true;
			}
			return false;
		}
		
		private boolean existsInCurrentDir(String[] currentDirFiles, String selectedFilesName)
		{
			for (int i = 0; i < currentDirFiles.length; i++)
			{
				if (currentDirFiles[i].equals(selectedFilesName))
					return true;
			}
			return false;
		}
		
		private void removeItemsFromTheList()
		{
			int[] selectedItems = list.getSelectedIndices();
			for (int i = 0; i < selectedItems.length; i++)
				// selectedItems[i]-i because ArrayList decreases the array each time you 
				// remove something from it. So, the max index is smaller and smaller.
				listModel.remove(selectedItems[i]-i);
			
			if (listModel.getSize() < 1)
			{
				bRemove.setEnabled(false);
				menuBar.getMenu(0).getMenuComponent(1).setEnabled(false);
				bZip.setEnabled(false);
				menuBar.getMenu(0).getMenuComponent(3).setEnabled(false);
			}
			
			labelCounts.setText("" + listModel.getSize());
		}
		
		private void createArchive()
		{
			fileChooser.setDialogTitle("Save archive as");
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setMultiSelectionEnabled(false);
			fileChooser.setSelectedFile(new File(lastPathUsedForSaving + File.separator + lastSavedName));
			fileChooser.addChoosableFileFilter(extensionFilter);
			fileChooser.setAcceptAllFileFilterUsed(false);
			
			int choice = fileChooser.showDialog(rootPane, "Save");
			
			if (choice == JFileChooser.APPROVE_OPTION)
			{
				byte[] tmpData = new byte[BUFFER];
				String[] dialogOptions = {"Replace", "Rename", "Cancel"};
				
				checkExtension();
				
				if (!existsInCurrentDir(fileChooser.getCurrentDirectory().list(), fileChooser.getSelectedFile().getName()))
					continueCreatingArchive(tmpData);
				else
				{
					JOptionPane dialogPane = new JOptionPane("File \"" + fileChooser.getSelectedFile().getName() + "\" already exists", JOptionPane.ERROR_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, dialogOptions);
					JDialog alreadyExistsDialog = dialogPane.createDialog(rootPane, "Zipper");
					alreadyExistsDialog.setVisible(true);
					
					if (dialogPane.getValue() == null)
						dialogPane.setValue(dialogOptions[2]);
					
					if (dialogPane.getValue().equals(dialogOptions[0]))
						continueCreatingArchive(tmpData);
					else if (dialogPane.getValue().equals(dialogOptions[1]))
					{
						renameTheFile();
						
						while(true)
						{
							if (!existsInCurrentDir(fileChooser.getCurrentDirectory().list(), fileChooser.getSelectedFile().getName()))
							{
								continueCreatingArchive(tmpData);
								break;
							}
							else
								renameTheFile();
						}
					}
				}
				
				lastPathUsedForSaving = fileChooser.getCurrentDirectory().getPath();
				lastSavedName = fileChooser.getSelectedFile().getName();
			}
		}
		
		private void checkExtension()
		{
			String currentName = fileChooser.getSelectedFile().getName();
			String currentPath = fileChooser.getCurrentDirectory().getPath();
			
			if (!currentName.endsWith(".zip"))
			{
				currentName = currentName.concat(".zip");
				fileChooser.setSelectedFile(new File(currentPath + File.separator + currentName));
			}
		}
		
		@SuppressWarnings("unchecked")
		private void continueCreatingArchive(byte[] tmpData)
		{
			int currentListElement = 0;
			
			try
			{
				ZipOutputStream zipOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(fileChooser.getSelectedFile()), BUFFER));
				
				for (int i = 0; i < listModel.getSize(); i++)
				{
					currentListElement = i;
					
					if ( !((File)listModel.get(i)).isDirectory() )
						zipThisEntry(zipOutS, (File)listModel.get(i), tmpData, ((File)listModel.get(i)).getPath() );
					else
					{
						provideAllPathsToBeZipped((File)listModel.get(i));
						
						for (int j = 0; j < pathList.size(); j++)
							zipThisEntry(zipOutS, (File)pathList.get(j), tmpData, ((File)listModel.get(i)).getPath() );
						
						pathList.removeAll(pathList);
					}
				}
				
				zipOutS.close();
			}
			catch(FileNotFoundException e)
			{
				JOptionPane.showMessageDialog(rootPane, "The filename, directory name, or volume label syntax is incorrect.\n" + fileChooser.getSelectedFile().getName(), "Zipper", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch(IOException e)
			{
				JOptionPane.showMessageDialog(rootPane, "Unknown IO Exception.", "Zipper", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
			catch(NullPointerException e)
			{
				JOptionPane.showMessageDialog(rootPane, "Error. Probably cannot access the file/directory.\n" + ((File)listModel.get(currentListElement)).getPath(), "Zipper", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
		
		private void zipThisEntry(ZipOutputStream zipOutS, File filePath, byte[] tmpData, String mainPath) throws IOException
		{
			BufferedInputStream inS = new BufferedInputStream(new FileInputStream(filePath), BUFFER);
			
			zipOutS.putNextEntry(new ZipEntry( filePath.getPath().substring(mainPath.lastIndexOf(File.separator)+1) ));
			
			int counter;
			while ( (counter = inS.read(tmpData, 0, BUFFER)) != -1)
				zipOutS.write(tmpData, 0, counter);
			
			zipOutS.closeEntry();
			inS.close();
		}
		
		private void renameTheFile()
		{
			String currentName = fileChooser.getSelectedFile().getName();
			String betweenParentheses;
			boolean isNumber = false;
			int renameNumber = 1;
			boolean addParentheses = true;
			
			if (currentName.contains("(") && currentName.contains(")"))
			{
				betweenParentheses = currentName.substring(currentName.lastIndexOf("(")+1, currentName.lastIndexOf(")"));
				char[] charsTocheck = betweenParentheses.toCharArray();
				
				for (int i = 0; i < charsTocheck.length; i++)
				{
					isNumber = Character.isDigit(charsTocheck[i]);
					if (isNumber)
						continue;
					else
						break;
				}
				
				if (isNumber)
				{
					int tempInt = Integer.parseInt(betweenParentheses);
					
					if (tempInt < Integer.MAX_VALUE)
					{
						renameNumber = tempInt + 1;
						addParentheses = false;
					}
				}
			}
			
			if (addParentheses)
			{
				currentName = currentName.substring(0, currentName.lastIndexOf("."));
				currentName = currentName + "(" + renameNumber + ").zip";
				fileChooser.setSelectedFile(new File(lastPathUsedForSaving + File.separator + currentName));
			}
			else
			{
				currentName = currentName.substring(0, currentName.lastIndexOf("."));
				currentName = currentName.substring(0, currentName.lastIndexOf("(")+1);
				currentName = currentName + renameNumber + ").zip";
				fileChooser.setSelectedFile(new File(lastPathUsedForSaving + File.separator + currentName));
			}
		}
		
		@SuppressWarnings("unchecked")
		private void provideAllPathsToBeZipped(File directory)
		{
			String[] allContents = directory.list();
			
			for (int i = 0; i < allContents.length; i++)
			{
				File f = new File(directory.getPath(), allContents[i]);
				
				if (f.isFile())
					pathList.add(f);
				
				if (f.isDirectory())
					provideAllPathsToBeZipped(new File(f.getPath()));
			}
		}

	}
	
	
	/**
	 * This class is supposed to emulate the behaviour of professional programs in regard to the File menu.
	 * Namely, pressing and releasing the Alt key activates the menu in the menu bar without using
	 * the mnemonic assigned to the menu.  
	 */
	private class MenuKeyListener implements KeyListener
	{
		boolean shouldMenuBeSelected;
		
		@Override
		public void keyPressed(KeyEvent e)
		{
			// When menu File is selected (highlighted) pressing Enter or one of below two arrows
			// should display all the menu items as when using Alt + F shortcut.
			shouldMenuBeSelected = true;
			
			if (menuBar.getMenu(0).isSelected())
				if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP)
					menuBar.getMenu(0).doClick();
		}
		
		@Override
		public void keyTyped(KeyEvent e)
		{
			// This prevents the menu from being selected when user executes any key shortcut
			// involving the Alt key.
			if (e.isAltDown() && !menuBar.getMenu(0).isSelected())
				shouldMenuBeSelected = false;
		}

		@Override
		public void keyReleased(KeyEvent e)
		{
			// It also shouldn't be selected when user closes any windows. Should have also 
			// prevented any other Alt + Fx combinations but didn't think of any useful ones,
			// so chances are small. And even if somebody uses e.g. Alt + F5 it doesn't really
			// matter that much.
			if (e.isAltDown() && e.getKeyCode() == KeyEvent.VK_F4)
				shouldMenuBeSelected = false;
			
			if (shouldMenuBeSelected == true && e.getKeyCode() == KeyEvent.VK_ALT)
			{
				if (menuBar.getMenu(0).isSelected())
					menuBar.getMenu(0).setSelected(false);
				else
					menuBar.getMenu(0).setSelected(true);
			}
		}
		
	}
	
	/**
	 * This class is used in place of the default JButton class solely for
	 * controlling the tool tip text size when scaling the window.
	 * When using the Action class to create a button it is probably
	 * the Action class which executes 'createToolTip' method.
	 */
	private class MyButton extends JButton
	{
		private Font tipFont = new Font(Font.DIALOG, Font.PLAIN, 12);
		
		public MyButton(Action action)
		{
			super(action);
		}
		public MyButton(Action action, Font font)
		{
			this(action);
			this.tipFont = font;
		}
		
		@Override
		public JToolTip createToolTip()
		{
			JToolTip tip = new JToolTip();
			tip.setFont(this.tipFont);
			
			return tip;
		}
	}
	
	
	private class MyMenuItem extends JMenuItem
	{
		private Font tipFont = new Font(Font.DIALOG, Font.PLAIN, 12);
		
		public MyMenuItem(Action action)
		{
			super(action);
		}
		public MyMenuItem(Action action, Font font)
		{
			this(action);
			this.tipFont = font;
		}
		
		@Override
		public JToolTip createToolTip()
		{
			JToolTip tip = new JToolTip();
			tip.setFont(this.tipFont);
			
			return tip;
		}
	}
	
}







