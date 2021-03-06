package com.codenjoy.dojo.loderunner.model;

import com.codenjoy.dojo.loderunner.services.LoderunnerEvents;
import com.codenjoy.dojo.services.Dice;
import com.codenjoy.dojo.services.EventListener;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * User: sanja
 * Date: 19.12.13
 * Time: 5:22
 */
public class SingleLoderunnerTest {

    private Dice dice;
    private EventListener listener1;
    private SingleLoderunner game1;
    private EventListener listener2;
    private SingleLoderunner game2;
    private Loderunner loderunner;
    private EventListener listener3;
    private SingleLoderunner game3;

    // появляется другие игроки, игра становится мультипользовательской
    @Test
    public void shouldMultipleGame() { // TODO разделить тест на части
        setupGm("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼####☼" +
                "☼   $☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        setupPlayer1(1, 4);
        setupPlayer2(2, 2);
        setupPlayer3(3, 4);

        String expected =
                "☼☼☼☼☼☼\n" +
                "☼► ► ☼\n" +
                "☼####☼\n" +
                "☼ ► $☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n";

        assertEquals(expected, game1.getBoardAsString());
        assertEquals(expected, game2.getBoardAsString());
        assertEquals(expected, game3.getBoardAsString());

        game1.getJoystick().right();
        game2.getJoystick().left();
        game3.getJoystick().right();

        game1.tick(); // достаточно тикнуть у одной доски

        expected =
                "☼☼☼☼☼☼\n" +
                "☼ ► ►☼\n" +
                "☼####☼\n" +
                "☼◄  $☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n";

        assertEquals(expected, game1.getBoardAsString());
        assertEquals(expected, game2.getBoardAsString());
        assertEquals(expected, game3.getBoardAsString());

        game1.getJoystick().act();
        game3.destroy();

        game1.tick();

        expected =
                "☼☼☼☼☼☼\n" +
                "☼ R. ☼\n" +
                "☼##*#☼\n" +
                "☼◄  $☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n";

        atGame1(expected);
        atGame2(expected);
        atGame3(expected);

        game1.getJoystick().right();

        game1.tick();
        game1.tick();
        game1.tick();

        expected =
                "☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼## #☼\n" +
                "☼◄ ►$☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n";

        atGame1(expected);
        atGame2(expected);

        game1.getJoystick().left();
        game1.getJoystick().act();
        game2.getJoystick().right();

        game1.tick();

        expected =
                "☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼## #☼\n" +
                "☼ [Я$☼\n" +
                "☼#*##☼\n" +
                "☼☼☼☼☼☼\n";

        atGame1(expected);
        atGame2(expected);

        for (int c = 2; c < Brick.DRILL_TIMER; c++) {
            game1.tick();
        }

        game1.tick();

        expected =
                "☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼####☼\n" +
                "☼  Я$☼\n" +
                "☼#Ѡ##☼\n" +
                "☼☼☼☼☼☼\n";

        atGame1(expected);
        atGame2(expected);

        verify(listener2).event(LoderunnerEvents.KILL_HERO);
        verify(listener1).event(LoderunnerEvents.KILL_ENEMY);
        assertTrue(game2.isGameOver());

        when(dice.next(anyInt())).thenReturn(1, 4);

        game2.newGame();

        game1.tick();

        expected =
                "☼☼☼☼☼☼\n" +
                "☼►   ☼\n" +
                "☼####☼\n" +
                "☼  Я$☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n";

        atGame1(expected);
        atGame2(expected);

        game1.getJoystick().right();

        when(dice.next(anyInt())).thenReturn(1, 2);

        game1.tick();

        expected =
                "☼☼☼☼☼☼\n" +
                "☼►   ☼\n" +
                "☼####☼\n" +
                "☼$  ►☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n";

        atGame1(expected);
        atGame2(expected);

        verify(listener1).event(LoderunnerEvents.GET_GOLD);

        assertEquals(1, game1.getCurrentScore());
        assertEquals(1, game1.getMaxScore());

        assertEquals(0, game2.getCurrentScore());
        assertEquals(0, game2.getMaxScore());

        game1.clearScore();

        assertEquals(0, game1.getCurrentScore());
        assertEquals(0, game1.getMaxScore());
    }

    // можно ли проходить героям друг через дурга? Нет
    @Test
    public void shouldICantGoViaAnotherPlayer_whenAtWay() {
        setupGm("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        setupPlayer1(1, 2);
        setupPlayer2(2, 2);

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼    ☼\n" +
                "☼►►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().right();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼    ☼\n" +
                "☼►►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game2.getJoystick().left();
        game2.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼    ☼\n" +
                "☼►◄  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");
    }

    @Test
    public void shouldICantGoViaAnotherPlayer_whenAtLadder() {
        setupGm("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ H  ☼" +
                "☼ H  ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        setupPlayer1(1, 2);
        setupPlayer2(2, 4);

        atGame1("☼☼☼☼☼☼\n" +
                "☼ ►  ☼\n" +
                "☼ H  ☼\n" +
                "☼►H  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().right();
        game2.getJoystick().down();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ Y  ☼\n" +
                "☼ Y  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().up();
        game2.getJoystick().down();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ Y  ☼\n" +
                "☼ Y  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().up();
        game2.getJoystick().down();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ Y  ☼\n" +
                "☼ Y  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");
    }

    @Test
    public void shouldICantGoViaAnotherPlayer_whenAtPipe() {
        setupGm("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼ ~~ ☼" +
                "☼#  #☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        setupPlayer1(1, 3);
        setupPlayer2(4, 3);

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼►~~►☼\n" +
                "☼#  #☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().right();
        game2.getJoystick().left();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ }{ ☼\n" +
                "☼#  #☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().right();
        game2.getJoystick().left();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ }{ ☼\n" +
                "☼#  #☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().right();
        game2.getJoystick().left();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ }{ ☼\n" +
                "☼#  #☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");
    }

    // могу ли я сверлить под другим героем? Нет
    @Test
    public void shouldICantDrillUnderOtherHero() {
        setupGm("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼►►  ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        setupPlayer1(1, 2);
        setupPlayer2(2, 2);

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼    ☼\n" +
                "☼►►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().act();
        game2.getJoystick().left();
        game2.getJoystick().act();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼    ☼\n" +
                "☼►◄  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");
    }

    // если я прыгаю сверху на героя, то я должен стоять у него на голове
    @Test
    public void shouldICantStayAtOtherHeroHead() {
        setupGm("☼☼☼☼☼☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼    ☼" +
                "☼####☼" +
                "☼☼☼☼☼☼");

        setupPlayer1(2, 4);
        setupPlayer2(2, 2);

        atGame1("☼☼☼☼☼☼\n" +
                "☼ [  ☼\n" +
                "☼    ☼\n" +
                "☼ ►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ ►  ☼\n" +
                "☼ ►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");
    }

    private void atGame1(String expected) {
        assertEquals(expected, game1.getBoardAsString());
    }

    private void atGame2(String expected) {
        assertEquals(expected, game2.getBoardAsString());
    }

    private void atGame3(String expected) {
        assertEquals(expected, game3.getBoardAsString());
    }

    private void setupPlayer2(int x, int y) {
        listener2 = mock(EventListener.class);
        game2 = new SingleLoderunner(loderunner, listener2);
        when(dice.next(anyInt())).thenReturn(x, y);
        game2.newGame();
    }

    private void setupPlayer3(int x, int y) {
        listener3 = mock(EventListener.class);
        game3 = new SingleLoderunner(loderunner, listener3);
        when(dice.next(anyInt())).thenReturn(x, y);
        game3.newGame();
    }

    private void setupPlayer1(int x, int y) {
        listener1 = mock(EventListener.class);
        game1 = new SingleLoderunner(loderunner, listener1);
        when(dice.next(anyInt())).thenReturn(x, y);
        game1.newGame();
    }

    private void setupGm(String map) {
        Level level = new LevelImpl(map);
        dice = mock(Dice.class);
        loderunner = new Loderunner(level, dice);
    }

    @Test
    public void shouldICanGoWhenIAmAtOtherHero() {
        shouldICantStayAtOtherHeroHead();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ ►  ☼\n" +
                "☼ ►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.getJoystick().left();
        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼]   ☼\n" +
                "☼ ►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game1.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼    ☼\n" +
                "☼◄►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");
    }

    @Test
    public void shouldICanGoWhenAtMeIsOtherHero() {
        shouldICantStayAtOtherHeroHead();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ ►  ☼\n" +
                "☼ ►  ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game2.getJoystick().right();
        game2.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼ [  ☼\n" +
                "☼  ► ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");

        game2.tick();

        atGame1("☼☼☼☼☼☼\n" +
                "☼    ☼\n" +
                "☼    ☼\n" +
                "☼ ►► ☼\n" +
                "☼####☼\n" +
                "☼☼☼☼☼☼\n");
    }
}
