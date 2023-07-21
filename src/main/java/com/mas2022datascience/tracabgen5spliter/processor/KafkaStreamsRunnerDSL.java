package com.mas2022datascience.tracabgen5spliter.processor;

import com.mas2022datascience.avro.v1.Object;
import com.mas2022datascience.avro.v1.PlayerBall;
import com.mas2022datascience.avro.v1.TracabGen5TF01;
import com.mas2022datascience.util.Player;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsRunnerDSL {

  @Value(value = "${spring.kafka.properties.schema.registry.url}")
  private String schemaRegistry;
  @Value(value = "${topic.tracab-01.name}")
  private String topicIn;
  @Value(value = "${topic.general-01.name}")
  private String topicOutPlayerBall;
  @Value(value = "${topic.general-02.name}")
  private String topicOutTeam;
  @Value(value = "${topic.general-03.name}")
  private String topicOutPlayerBallCompact;

  @Bean
  public KStream<String, TracabGen5TF01> kStream(StreamsBuilder kStreamBuilder) {

    // When you want to override serdes explicitly/selectively
    final Map<String, String> serdeConfig = Collections.singletonMap("schema.registry.url",
        schemaRegistry);
    final Serde<TracabGen5TF01> tracabGen5TF01Serde = new SpecificAvroSerde<>();
    tracabGen5TF01Serde.configure(serdeConfig, false); // `false` for record values

    KStream<String, TracabGen5TF01> stream = kStreamBuilder.stream(topicIn,
        Consumed.with(Serdes.String(), tracabGen5TF01Serde));

    KStream<String, PlayerBall> playerStream = stream.flatMap(
        (key, value) -> {
          List<KeyValue<String, PlayerBall>> result = new LinkedList<>();
          for (Object valueObject : value.getObjects()) {
            if (valueObject.getType() == 0 || valueObject.getType() == 1
                && valueObject.getPlayerId() != null) { // player
              result.add(KeyValue.pair(
                      key + "-" + valueObject.getPlayerId(),
                      PlayerBall
                          .newBuilder()
                          .setTs(value.getTs())
                          .setPlayerId(Player.getPlayerOrBallId(valueObject))
                          .setTeamId(valueObject.getTeamId())
                          .setMatchId(value.getMatchId())
                          .setIsBallInPlay(value.getIsBallInPlay())
                          .setBallPossession(value.getBallPossession())
                          .setZone(valueObject.getZone())
                          .setX(valueObject.getX())
                          .setY(valueObject.getY())
                          .setZ(valueObject.getZ())
                          .setVelocity(valueObject.getVelocity())
                          .setAccelleration(valueObject.getAccelleration())
                          .setDistance(valueObject.getDistance())
                          .setDistancePlayerBall(valueObject.getDistancePlayerBall())
                          .setPlayerBallVectorX(valueObject.getPlayerBallVectorX())
                          .setPlayerBallVectorY(valueObject.getPlayerBallVectorY())
                          .setPlayerBallVectorZ(valueObject.getPlayerBallVectorZ())
                          .setPlayerVectorX(valueObject.getPlayerVectorX())
                          .setPlayerVectorY(valueObject.getPlayerVectorY())
                          .setPlayerVectorZ(valueObject.getPlayerVectorZ())
                          .build()
                  )
              );
            } else {
              if (valueObject.getType() == 7) { // ball
                result.add(KeyValue.pair(
                        key + "-" + valueObject.getId(),
                        PlayerBall
                            .newBuilder()
                            .setTs(value.getTs())
                            .setPlayerId("0")
                            .setMatchId(value.getMatchId())
                            .setIsBallInPlay(value.getIsBallInPlay())
                            .setBallPossession(value.getBallPossession())
                            .setZone(valueObject.getZone())
                            .setX(valueObject.getX())
                            .setY(valueObject.getY())
                            .setZ(valueObject.getZ())
                            .setVelocity(valueObject.getVelocity())
                            .setAccelleration(valueObject.getAccelleration())
                            .setDistance(valueObject.getDistance())
                            .build()
                    )
                );
              }
            }
          }
          return result;
        }
    );
    playerStream.to(topicOutPlayerBall);

    // player ball compacted stream to topic
    stream.to(topicOutPlayerBallCompact);

    //playerStream.filter((key, value) -> value.getId().equals("250101463")).to("temp");

    return stream;

  }
}


