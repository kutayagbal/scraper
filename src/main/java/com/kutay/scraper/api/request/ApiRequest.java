package com.kutay.scraper.api.request;

import com.kutay.scraper.db.entity.site.ApiEndpoint;

public class ApiRequest {
    private ApiEndpoint apiEndpoint;
    private Object request;

    public ApiRequest(ApiEndpoint apiEndpoint, Object request) {
        this.apiEndpoint = apiEndpoint;
        this.request = request;
    }

    public Object getRequest() {
        return request;
    }

    public ApiEndpoint getApiEndpoint() {
        return apiEndpoint;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((apiEndpoint == null) ? 0 : apiEndpoint.hashCode());
        result = prime * result + ((request == null) ? 0 : request.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ApiRequest other = (ApiRequest) obj;
        if (apiEndpoint == null) {
            if (other.apiEndpoint != null)
                return false;
        } else if (!apiEndpoint.equals(other.apiEndpoint))
            return false;
        if (request == null) {
            if (other.request != null)
                return false;
        } else if (!request.equals(other.request))
            return false;
        return true;
    }

}
