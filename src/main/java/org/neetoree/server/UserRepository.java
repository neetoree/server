package org.neetoree.server;

import org.neetoree.server.orm.UserEntity;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Alexander <iamtakingiteasy> Tumin on 2016-12-09.
 */
public interface UserRepository extends CrudRepository<UserEntity, String> {
    UserEntity findByUsername(String username);
}
