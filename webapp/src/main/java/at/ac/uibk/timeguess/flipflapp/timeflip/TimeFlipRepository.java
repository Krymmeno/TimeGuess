package at.ac.uibk.timeguess.flipflapp.timeflip;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeFlipRepository extends JpaRepository<TimeFlip, Long> {

  Optional<TimeFlip> findByDeviceAddress(String address);
}
