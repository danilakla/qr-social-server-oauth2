package com.example.authserver.domain.service.nativeserivce;

import com.example.authserver.domain.dto.QRcodeInsertDto;
import com.example.authserver.domain.dto.QRcodeUpdateDto;
import com.example.authserver.domain.entity.ContentEntity;
import com.example.authserver.domain.entity.QREntity;
import com.example.authserver.domain.repository.QRRepository;
import com.example.authserver.domain.service.ContentService;
import com.example.authserver.domain.service.GenerationQRImageService;
import com.example.authserver.domain.service.QRcodeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class QRcodeServiceNativeImpl implements QRcodeService {

    public final QRRepository qrRepository;
    public final ContentService contentService;
    private  final GenerationQRImageService generationQRImageService;
//TODO insert content we must set trigger than find by profile maxsize of qr code
    public Object saveQRcode(QRcodeInsertDto qrCodeInsertDto, Long profileId) throws Exception {
       if(qrCodeInsertDto.getContents()==null){
           throw new Exception("bad");
       }
        var id = (Long) qrRepository.initQrCode(LocalDateTime.now(), qrCodeInsertDto.getDescription(), new byte[]{}, qrCodeInsertDto.getName(), profileId);
        var qrImage = generationQRImageService.generateQRImage(id);
        var entity = qrRepository.getQREntityById(id);
        entity.setImage(qrImage);
        var updatedEntity = saveContentsToQr(entity, qrCodeInsertDto.getContents());
        qrRepository.save(updatedEntity);

        return qrImage;

    }

    public Object deleteQRcodeById(Long id) throws Exception {
        try{

            var isSuccess = qrRepository.deleteQrCode(id);
            if((Long)isSuccess!=1){
                throw new Exception("dsad");
            }
            return isSuccess;
        }catch (SQLException exceptionHelper){
            throw  exceptionHelper;
        }
    }

    public Object updateQrCode(QRcodeUpdateDto qRcodeUpdateDto) throws Exception {
        var isSuccess = qrRepository.updatetQrCode(qRcodeUpdateDto.getId(), qRcodeUpdateDto.getDescription(), qRcodeUpdateDto.getName());
        if((Long)isSuccess!=1){
            throw new Exception("dsad");
        }
        return isSuccess;
    }
    private String getFileExtensionFromFileName(String fileName){
        int indexLastPoint=0;
        for (int i = 0; i < fileName.length(); i++) {
            if(fileName.charAt(i)=='.'){
                indexLastPoint=i;
            }
        }

        return  fileName.substring(indexLastPoint+1);
    }
    private QREntity saveContentsToQr(QREntity entity, List<MultipartFile> qrCodeInsertDto){

        for (var item :
                qrCodeInsertDto) {
            ContentEntity content = new ContentEntity();
            try {
                content.setData(item.getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            content.setFileName(item.getOriginalFilename());
            content.setExtension(getFileExtensionFromFileName(item.getOriginalFilename()));
            content.setQrCode(entity);
            entity.getContentEntities().add(content);
        }
        return  entity;
    }
    public Object addListContent(List<MultipartFile> contentInsertDtos, Long qrId) throws Exception {
        //TODO SOME WRONG WITH PERFORMANCE TIME
        if(contentInsertDtos==null){
            throw new Exception("bad");
        }
        var entity = qrRepository.getQREntityById(qrId);
        var updatedEntity = saveContentsToQr(entity, contentInsertDtos);
        qrRepository.save(updatedEntity);

        return  "good";
    }

    @Override
    public List<QREntity> getQrCodesById(Long id) {
        return qrRepository.getAllByProfileId(id);
    }

}
