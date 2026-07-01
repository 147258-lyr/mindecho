package com.mindecho.ui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class ScratchCardCanvas extends Canvas {
    private double canvasWidth;
    private double canvasHeight;
    private boolean scratched;
    private int scratchCount;
    
    public ScratchCardCanvas(double width, double height) {
        super(width, height);
        this.canvasWidth = width;
        this.canvasHeight = height;
        init();
    }
    
    private void init() {
        drawScratchOverlay();
        setupMouseEvents();
    }
    
    public void reset() {
        scratched = false;
        scratchCount = 0;
        drawScratchOverlay();
    }
    
    private void drawScratchOverlay() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.GRAY);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);
        
        gc.setFill(Color.LIGHTGRAY);
        for (int i = 0; i < canvasWidth; i += 8) {
            for (int j = 0; j < canvasHeight; j += 8) {
                if ((i + j) % 16 == 0) {
                    gc.fillRect(i, j, 4, 4);
                }
            }
        }
    }
    
    private void setupMouseEvents() {
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleScratch);
        addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleScratch);
    }
    
    private void handleScratch(MouseEvent e) {
        if (scratched) return;
        
        double x = e.getX();
        double y = e.getY();
        double radius = 20;
        
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(x - radius, y - radius, radius * 2, radius * 2);
        
        scratchCount++;
        
        if (scratchCount >= 20) {
            scratched = true;
            if (onScratchComplete != null) {
                onScratchComplete.run();
            }
        }
    }
    
    public void reveal() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, canvasWidth, canvasHeight);
        scratched = true;
    }
    
    private Runnable onScratchComplete;
    
    public void setOnScratchComplete(Runnable onComplete) {
        this.onScratchComplete = onComplete;
    }
}
