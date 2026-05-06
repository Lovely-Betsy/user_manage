package com.xmd.usermanage.model.request;
import lombok.Data;

import java.io.Serializable;

@Data
public class UserSearchRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String userName;
}