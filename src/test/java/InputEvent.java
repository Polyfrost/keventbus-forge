/**
 * Simulates FML's InputEvent class to test superclass detection and posting.
 */
public class InputEvent {
    public static class MouseInputEvent extends InputEvent {}
    public static class KeyInputEvent extends InputEvent {}
}
