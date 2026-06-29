package com.zrws.approval.service;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.zrws.approval.domain.entity.SealConfig;
import com.zrws.approval.mapper.SealConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

/**
 * 电子签章服务
 * <p>负责电子签章配置管理和PDF签章功能
 */
@Slf4j
@Service
public class ElectronicSealService {

    @Autowired
    private SealConfigMapper sealConfigMapper;

    /**
     * 在PDF指定位置添加签章图片
     *
     * @param pdfPath  PDF文件路径
     * @param sealId   签章ID
     * @param x        X坐标
     * @param y        Y坐标
     * @param pageNum  页码（从1开始）
     * @return 添加签章后的PDF字节数组
     */
    public byte[] addSealImageToPdf(String pdfPath, Long sealId, float x, float y, int pageNum) {
        try {
            SealConfig seal = sealConfigMapper.selectById(sealId);
            if (seal == null) {
                throw new RuntimeException("签章不存在: " + sealId);
            }

            byte[] imageBytes = getSealImageBytes(seal.getSealImage());
            ImageData imageData = ImageDataFactory.create(imageBytes);
            Image sealImage = new Image(imageData);
            sealImage.setFixedPosition(pageNum, x, y);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(pdfPath);
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(reader, writer);
            Document document = new Document(pdfDoc);

            document.add(sealImage);
            document.close();

            log.info("PDF签章完成: sealId={}, page={}, x={}, y={}", sealId, pageNum, x, y);
            return outputStream.toByteArray();
        } catch (Exception e) {
            log.error("PDF签章失败: {}", e.getMessage(), e);
            throw new RuntimeException("PDF签章失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询所有签章列表
     *
     * @return 签章列表
     */
    public List<SealConfig> listSeals() {
        try {
            return sealConfigMapper.selectList(null);
        } catch (Exception e) {
            log.error("查询签章列表失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询签章列表失败: " + e.getMessage(), e);
        }
    }

    /**
     * 根据ID查询签章
     *
     * @param sealId 签章ID
     * @return 签章信息
     */
    public SealConfig getSealById(Long sealId) {
        try {
            return sealConfigMapper.selectById(sealId);
        } catch (Exception e) {
            log.error("查询签章失败: {}", e.getMessage(), e);
            throw new RuntimeException("查询签章失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建签章配置
     *
     * @param seal 签章信息
     * @return 创建后的签章信息
     */
    public SealConfig createSeal(SealConfig seal) {
        try {
            if (seal.getStatus() == null) {
                seal.setStatus(SealConfig.Status.ACTIVE.name());
            }
            sealConfigMapper.insert(seal);
            log.info("创建签章成功: sealId={}, sealName={}", seal.getSealId(), seal.getSealName());
            return seal;
        } catch (Exception e) {
            log.error("创建签章失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建签章失败: " + e.getMessage(), e);
        }
    }

    /**
     * 更新签章配置
     *
     * @param seal 签章信息
     */
    public void updateSeal(SealConfig seal) {
        try {
            sealConfigMapper.updateById(seal);
            log.info("更新签章成功: sealId={}", seal.getSealId());
        } catch (Exception e) {
            log.error("更新签章失败: {}", e.getMessage(), e);
            throw new RuntimeException("更新签章失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除签章（逻辑删除）
     *
     * @param sealId 签章ID
     */
    public void deleteSeal(Long sealId) {
        try {
            sealConfigMapper.deleteById(sealId);
            log.info("删除签章成功: sealId={}", sealId);
        } catch (Exception e) {
            log.error("删除签章失败: {}", e.getMessage(), e);
            throw new RuntimeException("删除签章失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取签章图片字节数组
     * 支持base64格式和文件路径两种方式
     *
     * @param sealImage 签章图片（base64或路径）
     * @return 图片字节数组
     */
    private byte[] getSealImageBytes(String sealImage) throws Exception {
        if (sealImage == null || sealImage.trim().isEmpty()) {
            throw new RuntimeException("签章图片不能为空");
        }

        if (sealImage.startsWith("data:image/")) {
            int commaIndex = sealImage.indexOf(",");
            if (commaIndex > 0) {
                String base64Data = sealImage.substring(commaIndex + 1);
                return Base64.getDecoder().decode(base64Data);
            }
        }

        File imageFile = new File(sealImage);
        if (imageFile.exists() && imageFile.isFile()) {
            return Files.readAllBytes(imageFile.toPath());
        }

        throw new RuntimeException("无法解析签章图片: 既不是有效的base64格式，也不是有效的文件路径");
    }
}
