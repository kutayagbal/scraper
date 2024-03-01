package com.kutay.scraper.util;

import com.kutay.scraper.db.entity.product.House;
import com.kutay.scraper.db.repo.HouseRepo;

public final class Constants {
    public static final String IMAGES_DIRECTORY = "images";

    public enum PRODUCT_TYPE {
        HOUSE(House.class, HouseRepo.class);

        private final Class productClass;
        private final Class repoClass;

        private PRODUCT_TYPE(Class productClass, Class repoClass) {
            this.productClass = productClass;
            this.repoClass = repoClass;

        }

        public Class getProductClass() {
            return productClass;
        }

        public Class getRepoClass() {
            return repoClass;
        }

    }

    public enum ACTION_TYPE {
        FOLLOWED, CONTACTED, VIEWED, OFFERED, UNFOLLOWED
    }

    public enum TRADE_TYPE {
        RENT, SALE
    }

    public enum PRODUCT_STATE {
        NEW, REMOVED
    }

    public enum API_RESPONSE_FUNCTION_TYPE {
        BY_CLASS, BY_TAG, BY_ATTRIBUTE, BY_ATTRIBUTE_VALUE, BY_OWN_TEXT, NEXT_SIBLING, SET_TEXT_IF_TAG, CHILDREN
    }

    public enum API_FUNCTION_INDEX_TYPE {
        FROM_START, FROM_END
    }

    public enum API_STRING_FUNCTION_TYPE {
        SPLIT, REPLACE, SUBSTRING
    }
}
