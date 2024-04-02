package cloud.igibgo.igibgobackend.entity;


import lombok.Data;

@Data
public class APIResponse<T>{
    private ResponseCodes code;// response code
    private String message;// response message
    private T data;// response data

    public APIResponse(ResponseCodes code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }
}
