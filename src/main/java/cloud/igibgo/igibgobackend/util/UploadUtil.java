package cloud.igibgo.igibgobackend.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.nio.file.FileStore;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class UploadUtil {


    // 设置用户身份信息。
    // SECRETID 和 SECRETKEY 请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi 进行查看和管理
    //用户的 SecretId，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参见 https://cloud.tencent.com/document/product/598/37140
    @Value("${cos.secretId}")
    private static String secretId;

    //用户的 SecretKey，建议使用子账号密钥，授权遵循最小权限指引，降低使用风险。子账号密钥获取可参见 https://cloud.tencent.com/document/product/598/37140
    @Value("${cos.secretKey}")
    private static String secretKey;

    // 创建 COSClient 实例，这个实例用来后续调用请求
    static COSClient createCOSClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);


        // ClientConfig 中包含了后续请求 COS 的客户端设置：
        ClientConfig clientConfig = new ClientConfig();

        // 设置 bucket 的地域
        // COS_REGION 请参见 https://cloud.tencent.com/document/product/436/6224
        clientConfig.setRegion(new Region("ap-shanghai"));


        // 设置请求协议, http 或者 https
        // 5.6.53 及更低的版本，建议设置使用 https 协议
        // 5.6.54 及更高版本，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);


        // 以下的设置，是可选的：

        // 设置 socket 读取超时，默认 30s
//        clientConfig.setSocketTimeout(30 * 1000);
        // 设置建立连接超时，默认 30s
//        clientConfig.setConnectionTimeout(30 * 1000);


        // 如果需要的话，设置 http 代理，ip 以及 port
//        clientConfig.setHttpProxyIp("httpProxyIp");
//        clientConfig.setHttpProxyPort(80);


        // 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }

    // 创建 TransferManager 实例，这个实例用来后续调用高级接口
    static TransferManager createTransferManager() {
        // 创建一个 COSClient 实例，这是访问 COS 服务的基础实例。
        // 详细代码参见本页: 简单操作 -> 创建 COSClient
        COSClient cosClient = createCOSClient();


        // 自定义线程池大小，建议在客户端与 COS 网络充足（例如使用腾讯云的 CVM，同地域上传 COS）的情况下，设置成16或32即可，可较充分的利用网络资源
        // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
        ExecutorService threadPool = Executors.newFixedThreadPool(32);


        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        TransferManager transferManager = new TransferManager(cosClient, threadPool);


        // 设置高级接口的配置项
        // 分块上传阈值和分块大小分别为 5MB 和 1MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(5 * 1024 * 1024);
        transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);

        return transferManager;
    }

    static void shutdownTransferManager(TransferManager transferManager) {
        // 指定参数为 true, 则同时会关闭 transferManager 内部的 COSClient 实例。
        // 指定参数为 false, 则不会关闭 transferManager 内部的 COSClient 实例。
        transferManager.shutdownNow(true);
    }

    private static boolean checkFileSize(File file) {
        long fileSizeInBytes = file.length();
        // if greater than 5G
        return fileSizeInBytes <= 5L * 1024 * 1024 * 1024;
    }

    // 存储桶的命名格式为 BucketName-APPID，此处填写的存储桶名称必须为此格式
    @Value("${cos.bucketName}")
    private static String bucketName;

    @Value("${cos.publicAccessUrl}")
    private static String publicAccessUrl;

    /**
     * 上传文件
     *
     * @param file     文件
     * @param fileName 文件名
     * @param path     路径
     * @throws CosServiceException 上传失败
     * @throws CosClientException  上传失败
     */
    public static String upload(File file, String fileName, String path) throws IllegalArgumentException {
        // 检查文件大小
        if (!checkFileSize(file)) {
            log.error("File size exceeds 5G");
            throw new IllegalArgumentException("File size exceeds 5G");
        }

        // 使用高级接口必须先保证本进程存在一个 TransferManager 实例，如果没有则创建
        // 详细代码参见本页：高级接口 -> 创建 TransferManager
        TransferManager transferManager = createTransferManager();

        // 本地文件路径
        String localFilePath = "/tmp-upload/" + fileName;
        File localFile = new File(localFilePath);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, path + fileName, localFile);

        // 设置存储类型（如有需要，不需要请忽略此行代码）, 默认是标准(Standard), 低频(standard_ia)
        // 更多存储类型请参见 https://cloud.tencent.com/document/product/436/33417
        putObjectRequest.setStorageClass(StorageClass.Standard_IA);

        //若需要设置对象的自定义 Headers 可参照下列代码,若不需要可省略下面这几行,对象自定义 Headers 的详细信息可参考 https://cloud.tencent.com/document/product/436/13361
//        ObjectMetadata objectMetadata = new ObjectMetadata();


        //若设置 Content-Type、Cache-Control、Content-Disposition、Content-Encoding、Expires 这五个字自定义 Headers，推荐采用 objectMetadata.setHeader()
        //自定义header尽量避免特殊字符，使用中文前请先手动对其进行URLEncode
//        objectMetadata.setHeader(key, value);
        //若要设置 “x-cos-meta-[自定义后缀]” 这样的自定义 Header，推荐采用
//        Map<String, String> userMeta = new HashMap<String, String>();
//        userMeta.put("x-cos-meta-[自定义后缀]", "value");
//        objectMetadata.setUserMetadata(userMeta);


//        putObjectRequest.withMetadata(objectMetadata);


        try {
            // 高级接口会返回一个异步结果Upload
            // 可同步地调用 waitForUploadResult 方法等待上传完成，成功返回 UploadResult, 失败抛出异常
            Upload upload = transferManager.upload(putObjectRequest);
            UploadResult uploadResult = upload.waitForUploadResult();
        } catch (CosServiceException e) {
            log.error("Failed to upload file in cos service", e);
        } catch (CosClientException e) {
            log.error("Failed to upload file in cos client", e);
        } catch (InterruptedException e) {
            log.error("File upload interrupted", e);
        }


        // 确定本进程不再使用 transferManager 实例之后，关闭即可
        // 详细代码参见本页：高级接口 -> 关闭 TransferManager
        shutdownTransferManager(transferManager);

        // 删除本地文件
        boolean delete = localFile.delete();
        if (!delete) {
            log.error("Failed to delete local file");
        }
        return publicAccessUrl + "/" + path + fileName;
    }
}
