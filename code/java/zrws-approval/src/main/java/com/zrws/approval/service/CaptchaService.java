package com.zrws.approval.service;

import com.zrws.approval.service.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码服务
 */
@Slf4j
@Service
public class CaptchaService {

    @Autowired
    private RedisService redisService;

    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final int CAPTCHA_EXPIRE_SECONDS = 180;
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;

    private static final char[] CHARS = "0123456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final Color[] COLORS = {
            new Color(59, 130, 246),
            new Color(16, 185, 129),
            new Color(245, 158, 11),
            new Color(239, 68, 68),
            new Color(139, 92, 246)
    };

    /**
     * 生成验证码
     * @return 包含 uuid 和 base64 图片的 map
     */
    public java.util.Map<String, String> generateCaptcha() {
        String uuid = java.util.UUID.randomUUID().toString().replace("-", "");
        String code = generateCode();
        String imageBase64 = generateImage(code);
        
        redisService.setEx(CAPTCHA_KEY_PREFIX + uuid, code, CAPTCHA_EXPIRE_SECONDS);
        
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("uuid", uuid);
        result.put("image", imageBase64);
        return result;
    }

    /**
     * 验证验证码
     */
    public boolean validateCaptcha(String uuid, String code) {
        if (uuid == null || code == null || uuid.isBlank() || code.isBlank()) {
            return false;
        }
        String storedCode = redisService.getString(CAPTCHA_KEY_PREFIX + uuid);
        if (storedCode == null) {
            return false;
        }
        redisService.del(CAPTCHA_KEY_PREFIX + uuid);
        return storedCode.equalsIgnoreCase(code);
    }

    /**
     * 生成验证码文本
     */
    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片
     */
    private String generateImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        g.setFont(new Font("Arial", Font.BOLD, 24));
        
        Random random = new Random();
        
        for (int i = 0; i < 8; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }
        
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));
            g.drawOval(random.nextInt(WIDTH), random.nextInt(HEIGHT), 2, 2);
        }
        
        char[] chars = code.toCharArray();
        int x = 10;
        for (char c : chars) {
            g.setColor(COLORS[random.nextInt(COLORS.length)]);
            
            double angle = (random.nextDouble() - 0.5) * 0.5;
            g.rotate(angle, x + 10, HEIGHT / 2);
            g.drawString(String.valueOf(c), x, HEIGHT / 2 + 8);
            g.rotate(-angle, x + 10, HEIGHT / 2);
            
            x += 25;
        }
        
        g.dispose();
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            log.error("生成验证码图片失败", e);
            return "";
        }
    }
}
