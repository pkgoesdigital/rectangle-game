import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for the game object. The game itself is a StdDraw window with an
 * infinite loop, so it can't be tested — but everything that decides what
 * the game *does* lives here and is pure logic: movement, wall bouncing,
 * freeze/unfreeze, the click counter, and collision.
 *
 * Coordinates: width/height are half-extents, because StdDraw.filledRectangle
 * takes half width and half height. The canvas is 0..100 in both directions.
 */
class MovingRectangleTest {

    /** A still rectangle at (x,y) with half-extents 5x5 and 1 click left. */
    private static MovingRectangle still(double x, double y) {
        return new MovingRectangle(x, y, 5, 5, 0, 0, 1);
    }

    @Nested
    @DisplayName("movement")
    class Movement {

        @Test
        @DisplayName("moves by its velocity each tick")
        void movesByVelocity() {
            MovingRectangle r = new MovingRectangle(50, 50, 5, 5, 2, 3, 1);
            r.animate();
            assertAll(
                () -> assertEquals(52, r.getX(), 1e-9),
                () -> assertEquals(53, r.getY(), 1e-9));
        }

        @Test
        @DisplayName("a frozen rectangle does not move")
        void frozenStaysPut() {
            MovingRectangle r = new MovingRectangle(50, 50, 5, 5, 2, 3, 1);
            r.freeze();
            r.animate();
            assertAll(
                () -> assertEquals(50, r.getX(), 1e-9),
                () -> assertEquals(50, r.getY(), 1e-9));
        }
    }

    @Nested
    @DisplayName("bouncing off the walls")
    class Bouncing {

        @Test
        @DisplayName("bounces off the right wall and stays in bounds")
        void bouncesOffRight() {
            // half-width 5, so the right edge touches x=100 when xCoord=95
            MovingRectangle r = new MovingRectangle(94, 50, 5, 5, 2, 0, 1);
            r.animate(); // would put the right edge at 101
            assertAll(
                () -> assertEquals(95, r.getX(), 1e-9, "pushed back inside"),
                () -> assertTrue(r.getXVelocity() < 0, "now heading left"));
        }

        @Test
        @DisplayName("bounces off the left wall and stays in bounds")
        void bouncesOffLeft() {
            MovingRectangle r = new MovingRectangle(6, 50, 5, 5, -2, 0, 1);
            r.animate();
            assertAll(
                () -> assertEquals(5, r.getX(), 1e-9),
                () -> assertTrue(r.getXVelocity() > 0));
        }

        @Test
        @DisplayName("bounces off the top wall")
        void bouncesOffTop() {
            MovingRectangle r = new MovingRectangle(50, 94, 5, 5, 0, 2, 1);
            r.animate();
            assertAll(
                () -> assertEquals(95, r.getY(), 1e-9),
                () -> assertTrue(r.getYVelocity() < 0));
        }

        @Test
        @DisplayName("bounces off the bottom wall")
        void bouncesOffBottom() {
            MovingRectangle r = new MovingRectangle(50, 6, 5, 5, 0, -2, 1);
            r.animate();
            assertAll(
                () -> assertEquals(5, r.getY(), 1e-9),
                () -> assertTrue(r.getYVelocity() > 0));
        }

        @Test
        @DisplayName("never leaves the canvas over a long run")
        void staysOnCanvasForever() {
            MovingRectangle r = new MovingRectangle(50, 50, 5, 5, 0.7, 1.3, 1);
            for (int i = 0; i < 5000; i++) {
                r.animate();
                assertTrue(r.getX() - 5 >= -1e-9 && r.getX() + 5 <= 100 + 1e-9,
                    "escaped horizontally at tick " + i + ": x=" + r.getX());
                assertTrue(r.getY() - 5 >= -1e-9 && r.getY() + 5 <= 100 + 1e-9,
                    "escaped vertically at tick " + i + ": y=" + r.getY());
            }
        }
    }

    @Nested
    @DisplayName("freeze / unfreeze")
    class Freezing {

        @Test
        @DisplayName("starts unfrozen, freezes, and unfreezes")
        void freezeRoundTrip() {
            MovingRectangle r = still(50, 50);
            assertFalse(r.isFrozen(), "should start unfrozen");
            r.freeze();
            assertTrue(r.isFrozen());
            r.unfreeze();
            assertFalse(r.isFrozen());
        }

        @Test
        @DisplayName("unfreezing gives it 1-3 fresh clicks")
        void unfreezeResetsClicks() {
            MovingRectangle r = still(50, 50);
            r.freeze();
            r.setRemainingClicks(0);
            r.unfreeze();
            int clicks = r.getRemainingClicks();
            assertTrue(clicks >= 1 && clicks <= 3, "clicks was " + clicks);
        }

        @Test
        @DisplayName("unfreezing something already moving does nothing")
        void unfreezeIsNoOpWhenNotFrozen() {
            MovingRectangle r = still(50, 50);
            r.setRemainingClicks(2);
            r.unfreeze();
            assertEquals(2, r.getRemainingClicks(), "clicks should be untouched");
        }
    }

    @Nested
    @DisplayName("clicks")
    class Clicks {

        @Test
        @DisplayName("remembers the count it was built with")
        void remembersInitialClicks() {
            assertEquals(3, new MovingRectangle(50, 50, 5, 5, 0, 0, 3).getRemainingClicks());
        }

        @Test
        @DisplayName("reset always lands in 1..3")
        void resetIsInRange() {
            MovingRectangle r = still(50, 50);
            for (int i = 0; i < 200; i++) {
                r.resetRemainingClicks();
                int c = r.getRemainingClicks();
                assertTrue(c >= 1 && c <= 3, "clicks out of range: " + c);
            }
        }
    }

    @Nested
    @DisplayName("collision")
    class Collision {

        @Test
        @DisplayName("overlapping rectangles collide")
        void overlapCollides() {
            assertTrue(still(50, 50).collidesWith(still(52, 52)));
        }

        @Test
        @DisplayName("distant rectangles do not collide")
        void farApartDoesNotCollide() {
            assertFalse(still(10, 10).collidesWith(still(90, 90)));
        }

        @Test
        @DisplayName("collision is symmetric")
        void collisionIsSymmetric() {
            MovingRectangle a = still(50, 50);
            MovingRectangle b = still(58, 50);
            assertEquals(a.collidesWith(b), b.collidesWith(a));
        }

        @Test
        @DisplayName("touching exactly at the edge does not count as a collision")
        void touchingEdgesDoNotCollide() {
            // half-width 5 each: centres 10 apart means edges meet at x=55
            assertFalse(still(50, 50).collidesWith(still(60, 50)));
        }

        @Test
        @DisplayName("a rectangle collides with itself")
        void collidesWithItself() {
            MovingRectangle r = still(50, 50);
            assertTrue(r.collidesWith(r));
        }
    }

    @Nested
    @DisplayName("mouse hit testing")
    class MouseHits {

        @Test
        @DisplayName("a click in the middle hits")
        void centreHits() {
            assertTrue(still(50, 50).hasMouse(50, 50));
        }

        @Test
        @DisplayName("a click just inside the corner hits")
        void insideCornerHits() {
            assertTrue(still(50, 50).hasMouse(54.9, 54.9));
        }

        @Test
        @DisplayName("a click outside misses")
        void outsideMisses() {
            assertAll(
                () -> assertFalse(still(50, 50).hasMouse(56, 50), "right of it"),
                () -> assertFalse(still(50, 50).hasMouse(50, 44), "below it"));
        }
    }

    @Nested
    @DisplayName("colour")
    class Colour {

        @Test
        @DisplayName("has a colour as soon as it is built")
        void hasColourImmediately() {
            // Regression: the 2018 constructor never called randomColor(), so
            // r/g/b stayed 0 and every rectangle was drawn black until it
            // happened to hit a wall.
            assertNotNull(still(50, 50).getColor(), "should be coloured before the first bounce");
        }

        @Test
        @DisplayName("bouncing off a wall recolours it")
        void bounceChangesColour() {
            MovingRectangle r = new MovingRectangle(94, 50, 5, 5, 2, 0, 1);
            java.awt.Color before = r.getColor();
            // 256^3 possibilities, so a same-colour draw is ~1 in 16.7M; try a
            // few bounces to make a false failure effectively impossible.
            boolean changed = false;
            for (int i = 0; i < 5 && !changed; i++) {
                r.animate();
                changed = !r.getColor().equals(before);
            }
            assertTrue(changed, "colour should change when it hits a wall");
        }
    }
}
