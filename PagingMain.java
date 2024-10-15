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
    Page page; // The page currently stored in this frame

    public Frame(int frameNumber) {
        this.frameNumber = frameNumber;
        this.page = null; // Initially, no page is loaded
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
    private int[] frameNumbers; // Maps page numbers to frame numbers
    private boolean[] validBit; // Indicates if the page is currently in memory

    public PageTable(int numberOfPages) {
        frameNumbers = new int[numberOfPages];
        validBit = new boolean[numberOfPages];
        Arrays.fill(validBit, false); // Initially, no pages are loaded in memory
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
        validBit[pageNumber] = false; // Invalidate the page entry
    }

    public void displayPageTable() {
        System.out.println("Page Table:");
        for (int i = 0; i < frameNumbers.length; i++) {
            if (validBit[i]) {
                System.out.println("Page " + i + " -> Frame " + frameNumbers[i]);
            } else {
                System.out.println("Page " + i + " -> Not loaded in memory");
            }
        }
        System.out.println();
    }
}

class PagingSimulation {
    private Frame[] memory;
    private PageTable pageTable;
    private int pageSize;
    private int memorySize;
    private int numberOfPages;
    private Queue<Integer> pageQueue; // FIFO queue for tracking loaded pages

    public PagingSimulation(int pageSize, int memorySize, int numberOfPages) {
        this.pageSize = pageSize;
        this.memorySize = memorySize;
        this.numberOfPages = numberOfPages;
        memory = new Frame[memorySize / pageSize];
        for (int i = 0; i < memory.length; i++) {
            memory[i] = new Frame(i); // Initialize all frames
        }
        pageTable = new PageTable(numberOfPages);
        pageQueue = new LinkedList<>();
    }

    public void accessMemory(int logicalAddress) {
        int pageNumber = logicalAddress / pageSize;
        int offset = logicalAddress % pageSize;

        System.out.println(
                "Accessing Logical Address: " + logicalAddress + " (Page " + pageNumber + ", Offset " + offset + ")");

        if (pageTable.isPageLoaded(pageNumber)) {
            int frameNumber = pageTable.getFrameNumber(pageNumber);
            System.out.println("Page " + pageNumber + " is in Frame " + frameNumber
                    + ". Accessing data at physical location (Frame " + frameNumber + ", Offset " + offset + ")");
        } else {
            handlePageFault(pageNumber);
            accessMemory(logicalAddress); // Retry the memory access after handling the page fault
        }
    }

    private void handlePageFault(int pageNumber) {
        System.out.println("Page fault! Loading page " + pageNumber + " into memory.");

        if (pageQueue.size() < memory.length) {
            // Memory has free frames, load the page
            loadPageIntoFrame(pageNumber, pageQueue.size());
        } else {
            // Memory is full, replace a page using FIFO
            int pageToReplace = pageQueue.poll();
            int frameToReplace = pageTable.getFrameNumber(pageToReplace);
            System.out.println(
                    "Replacing Page " + pageToReplace + " from Frame " + frameToReplace + " with Page " + pageNumber);
            pageTable.invalidatePage(pageToReplace);
            loadPageIntoFrame(pageNumber, frameToReplace);
        }
    }

    private void loadPageIntoFrame(int pageNumber, int frameNumber) {
        memory[frameNumber].loadPage(new Page(pageNumber));
        pageTable.updatePageTable(pageNumber, frameNumber);
        pageQueue.add(pageNumber); // Add page to the FIFO queue
    }

    public void displayMemory() {
        System.out.println("\nCurrent Memory State:");
        for (Frame frame : memory) {
            System.out.println(frame);
        }
        System.out.println();
    }

    public void displayPageTable() {
        pageTable.displayPageTable();
    }
}

public class PagingMain {
    public static void main(String[] args) {
        int pageSize = 4; // Size of each page in bytes
        int memorySize = 16; // Total memory size in bytes (4 frames)
        int numberOfPages = 8; // Total number of pages

        PagingSimulation simulation = new PagingSimulation(pageSize, memorySize, numberOfPages);

        // Simulate memory accesses
        int[] logicalAddresses = { 1, 4, 9, 12, 8, 13, 20, 24, 4, 28 };
        for (int address : logicalAddresses) {
            simulation.accessMemory(address);
            simulation.displayMemory();
            simulation.displayPageTable();
            System.out.println("----------------------------------------");
        }
    }
}
