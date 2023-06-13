# Jolly Music Player
![feature_graphic](https://github.com/sptallent/jolly-music-player/assets/17508350/8c3e1865-5868-4884-baac-9783eab207d2)

## Description
A feature-rich music player written in native Java for the Android platform. This project uses the AcoustID project API along with MusicBrainz to accurately identify and tag meta-data associated with individual songs in the user's music library. 

## Getting Started
### Prerequisites
- minSdkVersion 23 (Android 6 Marshmallow)
- targetSdkVersion 33 (Android 13 Tiramisu)

## Features
- Seamless playback for uninterrupted music enjoyment.
- Automatic music metadata lookup for accurate song details.
- Intuitive organization for easy navigation and quick access to your favorite tunes.
- Smart search functionality to find songs, albums, or artists instantly.
- Stylish Light & Dark themes to customize the look and feel of the music player.
- Sleep timer to drift off to your favorite melodies.

## Installation
1. Clone the repository
```
git clone https://github.com/sptallent/jolly-music-player.git
```
2. Import the project into your favorite Java IDE.
3. Add your API key for AcoustID.
4. Build and run the application.

## Usage
1. Launch the Jolly Music Player application.
2. Browse and select your favorite songs, albums, or artists to start playing your music.
3. Explore the various features like automatic metadata lookup, intuitive organization, themes, and more.

## Known Issues:

1. Theme changing can cause the music service to be reset which requires the user to press next song before music will be played again.
2. Using the notification media controls while there isn't a song queue can cause the application to crash.
3. Input from external devices can send playNext and playPrev to the MusicService but play/pause doesn't seem to be working.

## Contributing
Contributions are welcome! If you have any ideas, suggestions, or bug reports, please open an issue or submit a pull request.

## License
This project is licensed under the [MIT License](LICENSE).

## Acknowledgements
- [AcoustID](https://acoustid.org/) - AcoustID is a project providing a unique identifier for audio recordings by analyzing their acoustic fingerprint.
- [MusicBrainz](https://musicbrainz.org/) - MusicBrainz is an open music encyclopedia that collects and makes available music metadata.
- [Glide](https://github.com/bumptech/glide) by Google - A fast and efficient image loading library for Android.
- [SmartTabLayout](https://github.com/ogaclejapan/SmartTabLayout) by Ogaclejapan - A custom ViewPager title strip that gives you a smooth and customizable tab layout.
- [AudioVisualizer](https://github.com/gautamchibde/android-audio-visualizer) by Gautam Chibde - A customizable audio visualizer for Android.
- [SeekArc](https://github.com/savantech/SeekArc) by SavanTech - A circular seek bar library for Android.

## Contact
For any questions or inquiries, please contact samuel.tallent00@gmail.com.

