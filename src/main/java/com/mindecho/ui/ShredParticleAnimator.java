package com.mindecho.ui;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShredParticleAnimator extends Canvas {
    private static final int PARTICLE_COUNT = 300;
    private static final int DURATION_MS = 500;
    
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();
    private AnimationTimer animationTimer;
    private long startTime;
    private Runnable onComplete;
    
    public ShredParticleAnimator() {
        this(400, 300);
    }
    
    public ShredParticleAnimator(double width, double height) {
        super(width, height);
    }
    
    public void startAnimation(Runnable onComplete) {
        this.onComplete = onComplete;
        createParticles();
        startTime = System.currentTimeMillis();
        
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                draw();
                
                if (System.currentTimeMillis() - startTime >= DURATION_MS) {
                    stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                }
            }
        };
        animationTimer.start();
    }
    
    private void createParticles() {
        particles.clear();
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;
        
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            Particle p = new Particle();
            p.x = centerX;
            p.y = centerY;
            p.vx = (random.nextDouble() - 0.5) * 10;
            p.vy = (random.nextDouble() - 0.5) * 10 - 2;
            p.size = 2 + random.nextDouble() * 6;
            p.alpha = 1.0;
            p.hue = random.nextDouble() * 360;
            particles.add(p);
        }
    }
    
    private void update() {
        double elapsed = (System.currentTimeMillis() - startTime) / (double) DURATION_MS;
        
        for (Particle p : particles) {
            p.x += p.vx;
            p.y += p.vy;
            p.vy += 0.2;
            p.alpha = 1.0 - elapsed;
        }
    }
    
    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());
        
        for (Particle p : particles) {
            gc.setGlobalAlpha(p.alpha);
            gc.setFill(Color.hsb(p.hue, 0.8, 0.8));
            gc.fillOval(p.x - p.size/2, p.y - p.size/2, p.size, p.size);
        }
        
        gc.setGlobalAlpha(1.0);
    }
    
    private static class Particle {
        double x, y;
        double vx, vy;
        double size;
        double alpha;
        double hue;
    }
}
