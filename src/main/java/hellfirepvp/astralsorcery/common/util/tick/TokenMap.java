package hellfirepvp.astralsorcery.common.util.tick;

import javax.annotation.Nonnull;
import java.util.HashMap;

/**
 * A HashMap where values carry a getValue() token contract.
 *
 * @param <K> key type
 * @param <V> value type, must implement MapToken
 */
public class TokenMap<K, V extends TokenMap.MapToken<?>> extends HashMap<K, V> {

    public interface MapToken<V> {
        @Nonnull
        V getValue();
    }
}
