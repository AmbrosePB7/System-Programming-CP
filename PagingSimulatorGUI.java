import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

class Page {
    int pageNumber;

    public Page(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    @Override
    public String toString() {
        return "Page " + pageNumber;
    }
}

class Frame {
    int frameNumber;
    Page page;

    public Frame(int frameNumber) {
        this.frameNumber = frameNumber;
        this.page = null;
    }

    public void loadPage(Page page) {
        this.page = page;
    }

    @Override
    public String toString() {
        if (page != null) {
            return "Frame " + frameNumber + ": " + page;
        } else {
            return "Frame " + frameNumber + ": Empty";
        }
    }
}

class PageTable {
    private int[] frameNumbers;
    private boolean[] validBit;

    public PageTable(int numberOfPages) {
        frameNumbers = new int[numberOfPages];
        validBit = new boolean[numberOfPages];
        Arrays.fill(validBit, false);
    }

    public void updatePageTable(int pageNumber, int frameNumber) {
        frameNumbers[pageNumber] = frameNumber;
        validBit[pageNumber] = true;
    }

    public int getFrameNumber(int pageNumber) {
        return frameNumbers[pageNumber];
    }

    public boolean isPageLoaded(int pageNumber) {
        return validBit[pageNumber];
    }

    public void invalidatePage(int pageNumber) {
        validBit[pageNumber] = false;
    }

    public String[] getTableData() {
        String[] data = new String[frameNumbers.length];
        for (int i = 0; i < frameNumbers.length; i++) {
            if (validBit[i]) {
                data[i] = "Page " + i + " -> Frame " + frameNumbers[i];
            } else {
                data[i] = "Page " + i + " -> Not loaded in memory";
            }
        }
        return data;
    }
}

class PagingSimulation {
    private Frame[] memory;
    private PageTable pageTable;
    private int pageSize;
    private int memorySize;
    private int numberOfPages;
    private Queue<Integer> pageQueue;
    private JTextArea log;

    public PagingSimulation(int pageSize, int memorySize, int numberOfPages, JTextArea log) {
        this.pageSize = pageSize;
        this.memorySize = memorySize;
        this.numberOfPages = numberOfPages;
        this.log = log;
        memory = new Frame[memorySize / pageSize];
        for (int i = 0; i < memory.length; i++) {
            memory[i] = new Frame(i);
        }
        pageTable = new PageTable(numberOfPages);
        pageQueue = new LinkedList<>();
    }

    public void accessMemory(int logicalAddress) {
        if (logicalAddress >= pageSize * numberOfPages) { // Fix for out-of-bounds access
            log.append("Error: Logical address " + logicalAddress + " exceeds process size.\n\n");
            return;
        }

        int pageNumber = logicalAddress / pageSize;
        int offset = logicalAddress % pageSize;

        log.append("Accessing Logical Address " + logicalAddress + "\n");
        log.append("Dividing the address: Page " + pageNumber + ", Offset " + offset + "\n");

        if (pageTable.isPageLoaded(pageNumber)) {
            int frameNumber = pageTable.getFrameNumber(pageNumber);
            log.append("Page " + pageNumber + " is in memory at Frame " + frameNumber + "\n");
            log.append("Accessing physical memory location (Frame " + frameNumber + ", Offset " + offset + ")\n\n");
        } else {
            log.append("Page Fault! Page " + pageNumber + " is not in memory.\n");
            handlePageFault(pageNumber);
            accessMemory(logicalAddress); // Recursive call after loading the page
        }
    }

    private void handlePageFault(int pageNumber) {
        log.append("Handling page fault: Loading page " + pageNumber + " into memory.\n");

        if (pageQueue.size() < memory.length) {
            loadPageIntoFrame(pageNumber, pageQueue.size());
        } else {
            int pageToReplace = pageQueue.poll();
            int frameToReplace = pageTable.getFrameNumber(pageToReplace);
            log.append("Replacing Page " + pageToReplace + " from Frame " + frameToReplace + " with Page " + pageNumber
                    + "\n");
            pageTable.invalidatePage(pageToReplace);
            loadPageIntoFrame(pageNumber, frameToReplace);
        }
    }

    private void loadPageIntoFrame(int pageNumber, int frameNumber) {
        memory[frameNumber].loadPage(new Page(pageNumber));
        pageTable.updatePageTable(pageNumber, frameNumber);
        pageQueue.add(pageNumber);
        log.append("Page " + pageNumber + " loaded into Frame " + frameNumber + "\n");
    }

    public void displayMemory(JTable memoryTable) {
        String[] memoryData = new String[memory.length];
        for (int i = 0; i < memory.length; i++) {
            memoryData[i] = memory[i].toString();
        }
        updateTable(memoryTable, memoryData);
    }

    public void displayPageTable(JTable pageTableDisplay) {
        String[] pageTableData = pageTable.getTableData();
        updateTable(pageTableDisplay, pageTableData);
    }

    private void updateTable(JTable table, String[] data) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);
        for (String row : data) {
            model.addRow(new Object[] { row });
        }
    }
}

public class PagingSimulatorGUI {
    private PagingSimulation simulation;
    private JTextArea log;
    private JTable memoryTable;
    private JTable pageTableDisplay;
    private JTextField processSizeField;
    private JTextField pageSizeField;

    public PagingSimulatorGUI() {
        JFrame frame = new JFrame("Paging Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        log = new JTextArea(10, 50);
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        memoryTable = new JTable(new DefaultTableModel(new Object[] { "Memory Frames" }, 0));
        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);

        pageTableDisplay = new JTable(new DefaultTableModel(new Object[] { "Page Table" }, 0));
        JScrollPane pageTableScrollPane = new JScrollPane(pageTableDisplay);

        JPanel inputPanel = new JPanel();
        JLabel processSizeLabel = new JLabel("Enter Process Size (Bytes):");
        processSizeField = new JTextField(10);
        JLabel pageSizeLabel = new JLabel("Enter Page Size (Bytes):");
        pageSizeField = new JTextField(10);
        JButton createButton = new JButton("Create Process");
        JButton accessButton = new JButton("Access Memory");
        JTextField logicalAddressField = new JTextField(10);

        inputPanel.add(processSizeLabel);
        inputPanel.add(processSizeField);
        inputPanel.add(pageSizeLabel);
        inputPanel.add(pageSizeField);
        inputPanel.add(createButton);
        inputPanel.add(logicalAddressField);
        inputPanel.add(accessButton);

        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(logScrollPane, BorderLayout.CENTER);
        frame.add(memoryScrollPane, BorderLayout.WEST);
        frame.add(pageTableScrollPane, BorderLayout.EAST);

        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int processSize;
                int pageSize;
                try {
                    processSize = Integer.parseInt(processSizeField.getText());
                    pageSize = Integer.parseInt(pageSizeField.getText());
                } catch (NumberFormatException ex) {
                    log.append("Please enter valid sizes for process and page!\n");
                    return;
                }

                int numberOfPages = (int) Math.ceil((double) processSize / pageSize);
                int memorySize = 16; // Memory size (fixed)
                simulation = new PagingSimulation(pageSize, memorySize, numberOfPages, log);
                log.append("Process created with size: " + processSize + " bytes, divided into " + numberOfPages
                        + " pages.\n");
            }
        });

        accessButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (simulation == null) {
                    log.append("Please create a process first!\n");
                    return;
                }
                int logicalAddress;
                try {
                    logicalAddress = Integer.parseInt(logicalAddressField.getText());
                } catch (NumberFormatException ex) {
                    log.append("Please enter a valid logical address!\n");
                    return;
                }
                simulation.accessMemory(logicalAddress);
                simulation.displayMemory(memoryTable);
                simulation.displayPageTable(pageTableDisplay);
            }
        });

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PagingSimulatorGUI());
    }
}
