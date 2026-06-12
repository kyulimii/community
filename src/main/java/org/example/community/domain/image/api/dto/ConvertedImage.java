package org.example.community.domain.image.api.dto;

import java.io.File;

public record ConvertedImage(
        File jpgFile,
        File webpFile
) {}