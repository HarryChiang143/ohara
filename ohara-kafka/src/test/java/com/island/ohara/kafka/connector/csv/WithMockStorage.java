/*
 * Copyright 2019 is-land
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.island.ohara.kafka.connector.csv;

import com.island.ohara.common.exception.OharaException;
import com.island.ohara.kafka.connector.storage.Storage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Iterator;
import java.util.stream.Collectors;
import org.junit.rules.TemporaryFolder;

public abstract class WithMockStorage extends CsvSinkTestBase {
  protected Storage storage;

  @Override
  public void setUp() {
    super.setUp();
    storage = new MockStorage();
  }

  protected File createTemporaryFolder() {
    try {
      TemporaryFolder folder = new TemporaryFolder();
      folder.create();
      return folder.getRoot();
    } catch (IOException e) {
      throw new OharaException(e);
    }
  }

  protected Collection<String> readData(String path) {
    InputStream in = storage.open(path);
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    return reader.lines().collect(Collectors.toList());
  }

  static class MockStorage implements Storage {
    @Override
    public Iterator<Path> list(String dirPath) {
      try {
        return Files.list(Paths.get(dirPath)).iterator();
      } catch (IOException e) {
        throw new OharaException(e);
      }
    }

    @Override
    public OutputStream create(String filePathAndName) {
      try {
        Path path = Paths.get(filePathAndName);
        Path parent = path.getParent();
        if (!Files.exists(parent)) {
          Files.createDirectories(parent);
        }
        return Files.newOutputStream(Paths.get(filePathAndName));
      } catch (IOException e) {
        throw new OharaException(e);
      }
    }

    @Override
    public OutputStream append(String filePathAndName) {
      throw new UnsupportedOperationException();
    }

    @Override
    public InputStream open(String filePathAndName) {
      try {
        return Files.newInputStream(Paths.get(filePathAndName));
      } catch (IOException e) {
        throw new OharaException(e);
      }
    }

    @Override
    public boolean exists(String filePath) {
      return Files.exists(Paths.get(filePath));
    }

    public boolean copy(boolean delSrc, boolean overwrite, String sourcePath, String targetPath) {
      try {
        Path copied = Paths.get(targetPath);
        Path originalPath = Paths.get(sourcePath);

        if (overwrite) {
          Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        } else {
          Files.copy(originalPath, copied);
        }

        if (delSrc) {
          Files.delete(originalPath);
        }

        return true;
      } catch (IOException e) {
        throw new OharaException(e);
      }
    }

    @Override
    public boolean move(String sourcePath, String targetPath) {
      try {
        Files.move(Paths.get(sourcePath), Paths.get(targetPath));
        return true;
      } catch (IOException e) {
        throw new OharaException(e);
      }
    }

    @Override
    public void delete(String filePathAndName) {
      try {
        Files.delete(Paths.get(filePathAndName));
      } catch (IOException e) {
        throw new OharaException(e);
      }
    }

    @Override
    public void mkdirs(String path) {
      try {
        Files.createDirectories(Paths.get(path));
      } catch (IOException e) {
        throw new OharaException(e);
      }
    }

    @Override
    public void close() {
      // Do nothing
    }
  }
}
