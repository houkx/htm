package htm;

/**
 *
 * @author davec
 * @param <T>
 */
public interface OutputProvider<T extends Input<?>> {
    public String getId();
    public T getOutput();
    public boolean isOutputActive();
}
