package at.ac.uibk.timeguess.flipflapp;

import at.ac.uibk.timeguess.flipflapp.game.round.MaxPoints;
import at.ac.uibk.timeguess.flipflapp.term.Term;
import at.ac.uibk.timeguess.flipflapp.term.TermRepository;
import at.ac.uibk.timeguess.flipflapp.topic.Topic;
import at.ac.uibk.timeguess.flipflapp.topic.TopicRepository;
import at.ac.uibk.timeguess.flipflapp.user.User;
import at.ac.uibk.timeguess.flipflapp.user.UserRepository;
import at.ac.uibk.timeguess.flipflapp.user.UserRole;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestDataConfiguration {

  public interface RANDOM_ADMIN {

    String USERNAME = "randomadmin";
    String FIRSTNAME = "random";
    String LASTNAME = "admin";
    String PASSWORD = "password";
  }

  public interface RANDOM_GAMEMANAGER {

    String USERNAME = "randomgamemanager";
    String FIRSTNAME = "random";
    String LASTNAME = "gamemanager";
    String PASSWORD = "password";
  }

  public interface RANDOM_PLAYER {

    String USERNAME = "randomplayer";
    String FIRSTNAME = "random";
    String LASTNAME = "player";
    String PASSWORD = "password";
  }

  public interface RANDOM_INACTIVE_PLAYER {

    String USERNAME = "randominactiveplayer";
    String FIRSTNAME = "random";
    String LASTNAME = "player";
    String PASSWORD = "password";
  }

  public interface SamplePlayer {

    String USERNAME = "SamplePlayer";
    String FIRSTNAME = "Sample";
    String LASTNAME = "Player";
    String PASSWORD = "password";
  }

  public interface SampleTopic {

    String NAME = "SampleTopic";
  }

  public interface SampleTerm {

    String NAME = "SampleTerm";
  }

  public interface RANDOM_TOPIC {

    String TOPIC_NAME = "topic";
    String TERM0_NAME = "term0";
    String TERM1_NAME = "term1";
  }

  public interface RANDOM_GAME_ROOM {

    String ROOM_NAME = "room";
    MaxPoints MAX_POINTS = MaxPoints.TEN;
  }

  @Bean
  public CommandLineRunner createTestUsers(final UserRepository userRepository,
      final PasswordEncoder passwordEncoder) {
    return args -> Stream
        .of(new User(RANDOM_ADMIN.USERNAME, RANDOM_ADMIN.FIRSTNAME, RANDOM_ADMIN.LASTNAME,
                passwordEncoder.encode(RANDOM_ADMIN.PASSWORD), UserRole.ADMIN, true),
            new User(RANDOM_GAMEMANAGER.USERNAME, RANDOM_GAMEMANAGER.FIRSTNAME,
                RANDOM_GAMEMANAGER.LASTNAME, passwordEncoder.encode(RANDOM_GAMEMANAGER.PASSWORD),
                UserRole.GAMEMANAGER, true),
            new User(RANDOM_PLAYER.USERNAME, RANDOM_PLAYER.FIRSTNAME, RANDOM_PLAYER.LASTNAME,
                passwordEncoder.encode(RANDOM_PLAYER.PASSWORD), UserRole.PLAYER, true),
            new User(RANDOM_INACTIVE_PLAYER.USERNAME, RANDOM_INACTIVE_PLAYER.FIRSTNAME,
                RANDOM_INACTIVE_PLAYER.LASTNAME,
                passwordEncoder.encode(RANDOM_INACTIVE_PLAYER.PASSWORD), UserRole.PLAYER, false))
        .forEach(userRepository::save);
  }

  @Bean
  public CommandLineRunner createTestTopics(final TopicRepository topicRepository,
      final TermRepository termRepository) {

    return args -> {
      Topic topic30 = new Topic(SampleTopic.NAME + "_30", true);
      Topic topic10 = new Topic(SampleTopic.NAME + "_10", true);
      Topic topic0 = new Topic(SampleTopic.NAME + "_0", true);
      List<Term> terms30 = IntStream.rangeClosed(1, 30)
          .mapToObj(i -> new Term(SampleTerm.NAME + "_" + i, topic30, true))
          .collect(Collectors.toList());
      List<Term> terms10 = IntStream.rangeClosed(1, 10)
          .mapToObj(i -> new Term(SampleTerm.NAME + "_" + i, topic10, true))
          .collect(Collectors.toList());

      topicRepository.saveAll(List.of(topic30, topic10, topic0));
      terms30.forEach(termRepository::save);
      terms10.forEach(termRepository::save);

      Topic topic = topicRepository.save(new Topic(RANDOM_TOPIC.TOPIC_NAME, true));
      Stream.of(new Term(RANDOM_TOPIC.TERM0_NAME, topic, true),
          new Term(RANDOM_TOPIC.TERM1_NAME, topic, true)).forEach(termRepository::save);
    };
  }

  @Bean
  public CommandLineRunner createTestPlayers(final UserRepository userRepository,
      final PasswordEncoder passwordEncoder) {
    return args -> {
      List<User> players = IntStream.rangeClosed(1, 10)
          .mapToObj(i -> new User(SamplePlayer.USERNAME + "_" + i, SamplePlayer.FIRSTNAME + "_" + i,
              SamplePlayer.LASTNAME + "_" + i, (passwordEncoder.encode(SamplePlayer.PASSWORD)),
              UserRole.ADMIN, true))
          .collect(Collectors.toList());
      userRepository.saveAll(players);
    };
  }
}
