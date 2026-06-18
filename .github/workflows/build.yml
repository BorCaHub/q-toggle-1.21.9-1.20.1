name: Build Q Toggle (1.20.x & 1.21.x grouped)

on: [push, workflow_dispatch]

jobs:
  build:
    runs-on: ubuntu-latest
    strategy:
      fail-fast: false
      matrix:
        include:
          # Grup 1.20.x — dicompile terhadap versi TERTUA (1.20.1) supaya
          # symbol yang dipakai dijamin ada di seluruh rentang 1.20.1-1.20.6.
          - group:  "1.20.x"
            mc:     "1.20.1"
            fabric: "0.92.2+1.20.1"
            loader: "0.14.21"
            loom:   "1.4.2"
            java:   "17"
            gradle: "8.8"
            range:  ">=1.20.1 <1.21"

          # Grup 1.21.x — dicompile terhadap versi TERTUA (1.21) supaya
          # symbol yang dipakai dijamin ada di seluruh rentang 1.21-1.21.9.
          # Constructor KeyMapping yang berubah di 1.21.9 ditangani lewat
          # reflection di QToggleClient.java, bukan di sini.
          - group:  "1.21.x"
            mc:     "1.21"
            fabric: "0.100.1+1.21"
            loader: "0.15.11"
            loom:   "1.7.4"
            java:   "21"
            gradle: "8.8"
            range:  ">=1.21 <1.22"

    steps:
      - uses: actions/checkout@v4

      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: ${{ matrix.java }}

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: ${{ matrix.gradle }}

      - name: Generate Gradle wrapper
        run: gradle wrapper --gradle-version ${{ matrix.gradle }}

      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew

      - name: Override Gradle wrapper version
        run: |
          sed -i "s|distributionUrl=.*|distributionUrl=https\\://services.gradle.org/distributions/gradle-${{ matrix.gradle }}-bin.zip|" \
            gradle/wrapper/gradle-wrapper.properties

      - name: Override gradle.properties
        run: |
          sed -i "s/^minecraft_version=.*/minecraft_version=${{ matrix.mc }}/"   gradle.properties
          sed -i "s/^fabric_version=.*/fabric_version=${{ matrix.fabric }}/"     gradle.properties
          sed -i "s/^loader_version=.*/loader_version=${{ matrix.loader }}/"     gradle.properties
          sed -i "s/^loom_version=.*/loom_version=${{ matrix.loom }}/"           gradle.properties

      - name: Set Java release target sesuai grup
        run: sed -i "s/it.options.release = .*/it.options.release = ${{ matrix.java }}/" build.gradle

      - name: Set rentang versi Minecraft di fabric.mod.json
        run: sed -i "s/\"minecraft\": \">=1.20\"/\"minecraft\": \"${{ matrix.range }}\"/" src/main/resources/fabric.mod.json

      - name: Clean Gradle cache
        run: |
          rm -rf ~/.gradle/caches/modules-2/files-2.1/net.fabricmc/
          rm -rf .gradle/loom-cache/

      - name: Build mod
        run: ./gradlew clean build --info

      - name: Rename jar
        run: |
          mkdir -p staging
          for f in build/libs/*.jar; do
            [[ "$f" == *-sources* ]] && continue
            cp "$f" "staging/qtoggle-${{ matrix.group }}-1.0.0.jar"
          done

      - name: Upload jar
        uses: actions/upload-artifact@v4
        with:
          name: jar-${{ matrix.group }}
          path: staging/*.jar

  package:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - name: Download semua jar
        uses: actions/download-artifact@v4
        with:
          pattern: jar-*
          merge-multiple: true
          path: all-jars

      - name: Buat ZIP
        run: |
          cd all-jars
          zip -j "../Q Toggle 1.20.x + 1.21.x (1.0.0).zip" *.jar
          cd ..
          ls -lh "Q Toggle 1.20.x + 1.21.x (1.0.0).zip"

      - name: Upload ZIP final
        uses: actions/upload-artifact@v4
        with:
          name: "Q Toggle 1.20.x + 1.21.x (1.0.0)"
          path: "Q Toggle 1.20.x + 1.21.x (1.0.0).zip"
