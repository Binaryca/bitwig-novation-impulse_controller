# Bitwig Novation Impulse 25/49/61

Bitwig Studio controller extension for the **Novation Impulse 25, 49 and 61**
keyboards.

This repository is a locally maintained fork of
[tlipinski/bitwig-novation-impulse](https://github.com/tlipinski/bitwig-novation-impulse).
See [docs/CONTROLS.md](docs/CONTROLS.md) for the complete MIDI mapping.

## Features

- 8 track faders plus master fader (Impulse 49/61)
- Track mute/solo buttons with LED feedback
- Shift + track button for exclusive mute/solo
- Two encoder modes:
  - **Plugin** - controls the remote-control pages of the selected device
  - **Mixer** - controls pan and sends 1-6 of the eight mixer tracks
- Transport controls
- Clip launcher on the eight drum pads, including pad LED feedback
- Scene navigation and scene launch while the Bitwig Mix panel is open
- Track bank paging and cursor-track selection
- Impulse display feedback (track names, parameter names, page names and ranges)
- Data knob as a preset browser for the selected device (Program Change mode:
  Shift + MIDI Chan)
- Undo/redo (Shift + rewind/fast-forward) and clearing all mutes/solos
  (Shift + mute/solo mode button)
- Transport button LED feedback for play, record and loop
- Sustain, modulation and expression pedal support through Bitwig note input
- SysEx template sender in the Bitwig controller preferences, optional
  automatic template send at startup

## Changes in 1.3

- Extended the mixer encoder pages from pan + 2 sends to **pan + 6 sends**
- Added **data knob preset browsing**: in Program Change mode (Shift + MIDI
  Chan) the data knob moves the device browser through the presets of the
  selected device and loads the highlighted preset after a short pause
- Verified the master fader controls the master track on Impulse 49/61
- Added automatic template send at startup (**Send template at startup**
  preference, on by default)
- Added automatic template send at startup (**Send template at startup**
  preference, on by default)
- Added transport LED feedback: Play, Record and Loop light up according to the
  Bitwig transport state
- Added Shift + Rewind = Undo and Shift + Fast-forward = Redo with display
  feedback
- Added Shift + Mute/Solo mode button to clear all mutes or all solos of the
  visible tracks
- The display shows `Ready` (and the model number) when the Impulse answers
  the device inquiry at startup

## Changes in 1.2

- Fixed clip-launch pad LED feedback so only the current scene drives the LEDs
- Added a one-scene track bank so scene navigation and clip launch always refer
  to the same scene window
- Added Shift + Scene Launch to stop all clips
- Added scene number feedback on the display
- Fixed double handling of button release messages for transport, encoder and
  track-bank buttons
- Replaced the stream-based command matcher with a simple boolean check
- Added MIDI mapping constants and removed duplicated/dead code
- Added unit tests and Windows build helpers (`build.cmd`, `build.ps1`)
- Added optional persistent control labels (`67 05`) and numeric value display
  (`67 09`)
- Added [docs/HARDWARE.md](docs/HARDWARE.md) with the full hardware and SysEx
  reference
- Updated to Bitwig Extension API 25
- Documented the complete control mapping in [docs/CONTROLS.md](docs/CONTROLS.md)

## Compatibility

- Built against **Bitwig Extension API 25** and requires a Bitwig Studio version
  that provides API 25 (Bitwig Studio 6.x)
- The extension identifies the connected model automatically through the
  Impulse SysEx welcome message

## Installation

1. Download the latest `NovationImpulse.bwextension` release, or build it from
   source (see below).
2. Copy `NovationImpulse.bwextension` to the Bitwig extensions directory:
   - **Windows:** `%USERPROFILE%\Documents\Bitwig Studio\Extensions`
   - **macOS:** `Documents/Bitwig Studio/Extensions`
   - **Linux:** `$HOME/Bitwig Studio/Extensions`
3. Restart Bitwig Studio.
4. Open **Settings > Controllers**, add the **Novation Impulse** controller and
   select both Impulse MIDI inputs if auto-detection does not do it for you.

## Setting the controller template

The extension sends a SysEx template to the Impulse. The template maps the
keyboard controls to the CC numbers expected by this script.

1. Using the **Data** knob, select an unused template. Templates **15-20** are
   blank by default.
2. Open the controller settings in Bitwig and press **Send template**.
3. Confirm the new template on the Impulse with **Save** (`Shift` + keyboard
   combination).
4. Set a name for the template using the **Data** knob.
5. Confirm by pressing the **Data** knob.

Select this template on the Impulse before using the controller script.

## Quick reference

| Control | Action |
| --- | --- |
| Faders 1-8 | Track volume |
| Master fader | Master volume (Impulse 49/61; cursor track on Impulse 25) |
| Track buttons 1-8 | Mute or solo, depending on the button mode |
| Shift + track button | Exclusive mute/solo |
| Encoders 1-8 | Selected device parameters (Plugin mode) or pan/sends (Mixer mode) |
| Encoder mode buttons | Switch between Plugin and Mixer encoder modes |
| Encoder page buttons | Previous/next remote-control page or mixer page |
| Transport | Rewind, fast-forward, stop, play, arranger loop, record |
| Shift + Rewind / Fast-forward | Undo / redo |
| Drum pads 1-8 | Launch clip in the current scene for tracks 1-8 |
| Shift + drum pad | Stop the corresponding track |
| Rewind/Forward in Mix view | Previous/next scene |
| Loop in Mix view | Launch the current scene |
| Shift + Loop in Mix view | Stop all clips |
| Track bank buttons | Scroll the track bank by pages |
| Track previous/next | Select the cursor track |
| Data knob (Prog Change mode) | Browse and load presets of the selected device |

The full mapping, including CC numbers, is in
[docs/CONTROLS.md](docs/CONTROLS.md). The hardware and SysEx reference is in
[docs/HARDWARE.md](docs/HARDWARE.md). The implementation roadmap is in
[docs/IMPLEMENTACE.md](docs/IMPLEMENTACE.md).

## Building from source

Requirements:

- JDK 11 or newer (JDK 21 is used for development)
- Maven 3.9 or newer

### Windows

The helper scripts locate a JDK and Maven in `JAVA_HOME`/`MAVEN_HOME`, on
`PATH`, or in `%USERPROFILE%\.local\tools`.

```powershell
.\build.cmd
```

To build and copy the extension directly into the Bitwig extensions folder:

```powershell
.\build.cmd -Install
```

`build.ps1` can be used directly from PowerShell 7. The `build.cmd` wrapper
also works when PowerShell script execution is disabled.

### macOS / Linux

```bash
mvn clean package
./install-extension.sh
```

The build output is `target/NovationImpulse.bwextension`.

### Tests

```bash
mvn test
```

## Preferences

The controller preferences contain:

- **Debug logging** - logs received MIDI messages and some controller events to
  the Bitwig controller console.
- **Control labels** - sends persistent eight-character labels for the fader,
  button and knob zones to the Impulse (`Vol 1`-`Vol 8`, `Master`, `Mute 1`-`Mute 8`,
  `Knob 1`-`Knob 8`, Novation SysEx command `67 05`). The hardware displays
  these labels when it runs in label mode; see [docs/HARDWARE.md](docs/HARDWARE.md).
- **Send template at startup** - sends the SysEx template automatically when the
  extension starts (on by default).
- **Send template** - sends the SysEx template described above on demand.

## Troubleshooting

### The controller is not detected

- Make sure both Impulse MIDI inputs are enabled for the controller.
- On Windows the input ports are usually `Novation Impulse` and
  `MIDIIN2 (Novation Impulse)`.
- If auto-detection fails, add the controller manually and select the same
  input/output ports.
- The extension queries the Impulse model over SysEx. If the model is not
  detected, the script falls back to the Impulse 25 layout until the welcome
  message arrives.

### Faders or pads do not control Bitwig

- Confirm that the template created by **Send template** is selected on the
  Impulse.
- Check the MIDI channel configuration in the template.
- Enable **Debug logging** and inspect the controller console to see whether
  MIDI messages arrive.

### The display shows stale characters

The extension pads display text to eight characters and clears the remaining
positions. If the display still looks wrong, send the template again and
restart the controller script.

### Build fails on Windows with a path/junction error

Use `build.cmd` or `build.ps1`. The scripts resolve directory junctions (for
example a localized `Dokumenty` folder) before invoking Maven, because `javac`
cannot read a classpath that goes through such a junction.

## Project structure

```
src/main/java/net/tlipinski/bitwig/controller/
  Midi.java                 MIDI CC mapping constants
  MidiCommand.java          command interface
  MidiCallback.java         dispatches incoming MIDI to commands
  NovationImpulseExtension  extension entry point
  Tracks.java               Bitwig track/scene/device model
  ClipLauncher.java         clip pad LED feedback
  commands/                 transport, mixer, encoder and clip commands
  observers/                Bitwig value observers and display callbacks
  Template.java             Impulse SysEx template
```

Tests live in `src/test/java`.

## Credits

Original project by [tlipinski](https://github.com/tlipinski/bitwig-novation-impulse).
