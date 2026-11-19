package classes;

/**
 * The colors of the balls in the Ball Sort Puzzle. Each name is the letter used in the input
 * ({@code R} - Red, {@code B} - Blue, {@code P} - Purple, {@code Y} - Yellow, {@code G} - Green).
 *
 * <p>{@code 'E'} (empty space) is not a color, so it is not here.
 *
 * <p>{@link #ordinal()} gives each color a number from 0 to 4, so it can be used as an array index.
 * {@code Color.valueOf("R")} turns a letter into its color and throws {@link IllegalArgumentException}
 * for any other letter.
 */
public enum Color {
    R, B, P, Y, G
}
