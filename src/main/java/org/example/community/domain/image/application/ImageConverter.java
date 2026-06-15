package org.example.community.domain.image.application;

import com.sksamuel.scrimage.ImmutableImage;
import com.sksamuel.scrimage.webp.WebpWriter;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.example.community.domain.image.api.dto.ConvertedImage;
import org.example.community.global.exception.CustomException;
import org.example.community.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ImageConverter {


    public ConvertedImage converter(MultipartFile file) {
        try {
            BufferedImage inputImage = ImageIO.read(file.getInputStream());

            File jpgFile = convertToJpg(inputImage);
            File webpFile = convertToWebp(inputImage);
            return new ConvertedImage(jpgFile, webpFile);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }

    // jpgPath로 변환
    public File convertToJpg(BufferedImage inputImage) throws IOException {
        File jpgFile = File.createTempFile("converted_", ".jpg");
        BufferedImage background = new BufferedImage(
                inputImage.getWidth(), inputImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = background.createGraphics();
        g.drawImage(inputImage, 0, 0, Color.WHITE, null);
        g.dispose();
        ImageIO.write(background, "jpg", jpgFile);
        return jpgFile;
    }


    // webpPath로 변환
    public File convertToWebp(BufferedImage inputImage) throws IOException {
        File webpFile = File.createTempFile("converted_", ".webp");
        ImmutableImage.fromAwt(inputImage)
                .output(WebpWriter.DEFAULT, webpFile);
        return webpFile;
    }

}
