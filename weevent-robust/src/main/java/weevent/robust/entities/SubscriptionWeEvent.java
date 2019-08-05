package weevent.robust.entities;


import com.webank.weevent.sdk.WeEvent;
import lombok.Data;

import java.io.Serializable;

/**
 * WeEvent with subscription ID.
 *
 * @author matthewliu
 * @since 2019/03/05
 */
@Data
public class SubscriptionWeEvent implements Serializable {
    private String subscriptionId;
    private WeEvent event;
}
