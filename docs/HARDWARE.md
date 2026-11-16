# Novation Impulse hardware reference

Hardware and MIDI specification of the Novation Impulse 25/49/61, compiled for
the development of this extension from official Novation documentation and from
community reverse engineering of the Novation SysEx protocol.

Sources:

- [Impulse downloads and User Guide V5 (English)](https://downloads.novationmusic.com/novation/impulse/impulse-61)
- [Impulse product page](https://novationmusic.com/products/impulse-61)
- [Impulse Getting Started Guide](https://fael-downloads-prod.focusrite.com/customer/prod/s3fs-public/downloads/impulse-getting-started-guide_0.pdf)
- Reverse engineered SysEx notes: [jkl1337/alsa-novation-impulse](https://github.com/jkl1337/alsa-novation-impulse)

The CC mapping actually used by this extension is documented in
[CONTROLS.md](CONTROLS.md).

## Hardware controls

### Assignable controls

- 61 note semi-weighted keyboard, velocity sensitive, **channel aftertouch**
  (High Rate Scan technology, 10 000 scans per second)
- 8 endless rotary encoders with speed dependent resolution
- 9 faders (8 track faders + master; the Impulse 25 has one fader)
- 9 buttons under the faders for mute/solo (not present on the Impulse 25)
- 8 velocity and pressure sensitive drum pads with tri-colour backlight
- Modulation wheel

### Non-assignable controls

- Multifunction LCD (75 x 32 mm, blue backlit) with an eight-character text
  area and a three-character numeric area
- `+` / `-` buttons (press both together for Help mode)
- Data knob (press = Enter)
- Template Edit buttons: **Keyboard**, **Controls**, **Zones**, **MIDI Chan**
  (all with LEDs), plus **Setup** and **Shift**
- **Plugin/MIDI** button pair for the encoders, **Mixer/MIDI** button pair for
  the faders and the **Mute/Solo** toggle for the fader buttons (49/61 only)
- Octave `+` / Octave `-` buttons (press both together for transpose)
- **Roll** and **Arp** buttons (press both together for Clip Launch mode)
- Pitch bend wheel
- Six transport buttons: **Rewind, Fast-Forward, Stop, Play, Loop, Record**;
  they are always active and send CC or MMC depending on the Setup setting

### Rear panel

- USB-B port (bus powered), optional DC power socket
- Sustain pedal jack and expression pedal jack (both 1/4")
- MIDI In and MIDI Out (5-pin DIN)
- Kensington lock

## MIDI implementation

| Function | Transmitted | Recognized |
| --- | --- | --- |
| Basic channel | 1-16 | 1-16 |
| Mode | Mode 3 (OMNI OFF, POLY) | - |
| Note numbers | 0-127 | 0-127 |
| Velocity, Note On/Off | yes | yes |
| Channel aftertouch | yes | yes |
| Pitch bend | yes | yes |
| Control change | 0-127 | 0-127 |
| Program change | 0-127 | 0-127 |
| System exclusive | firmware update, template data | same |
| Song position, song select, tune request | - | - |
| MIDI clock | yes | yes |
| Reset all controllers | yes | - |
| Local ON/OFF, active sensing, system reset | passthrough via DIN | yes |

## Assignable message types

- **Faders, encoders, mod wheel:** CC, RPN or NRPN with min/max, MIDI channel
  (1-16 or template) and port (template / USB / DIN / all)
- **Drum pads:** note (C-2 to G8) or CC/RPN/NRPN
- **Buttons:** CC, RPN, NRPN or program change with bank MSB/LSB and one of the
  button types:
  - `sgl` – sends a single value on press
  - `Mty` – momentary press and release values
  - `Tgl` – toggle on/off values
  - `StP` – step: increments from a minimum to a maximum by a step size

## Global settings (Setup mode)

| Page | Setting | Values |
| --- | --- | --- |
| 1 | Transport | MMC (`MC`) or CC (`CC`) |
| 2 | Pad velocity curve | 1-3, or Off (fixed 127) |
| 3 | Tempo | 40-240 BPM (internal clock) |
| 4 | Clock source | Internal, USB, MIDI, Auto |
| 5 | MIDI Out source | Local or USB |
| 6 | SysEx dump | Dumps the current template |

Keyboard settings (per template): MIDI port (USB / DIN / All), velocity curve
1-4 or Off (fixed 127), aftertouch on/off. The MIDI channel is part of the
template; Shift + MIDI Chan opens Program Change mode.

The keyboard can be split into **four zones**, each with its own start/end
note, octave, MIDI channel and port.

## Built-in hardware modes (HUI / Clip Launch)

The Impulse firmware contains two built-in DAW modes. They belong to the
keyboard itself; this extension does not use them directly, but it replicates
the Ableton-style scene behaviour inside Bitwig - see
[CONTROLS.md](CONTROLS.md).

### HUI mode

- The Impulse switches to HUI automatically when a DAW sends HUI heartbeats,
  and back after five seconds without them. HUI uses its own MIDI port (on
  Windows `MIDIIN2`/`MIDIIN3` named `Impulse HUI`).
- Bitwig does not use HUI with this extension; the extension talks to the
  keyboard through the Novation SysEx control mode instead.
- Faders control volume, encoders control pan, the fader buttons mute/solo and
  the transport buttons work as usual.
- Shift + Octave down/up selects the previous/next track.
- Shift + Mixer/MIDI moves the fader bank up/down by eight channels.

### Clip Launch mode (Ableton-style)

- Entered by pressing Roll and Arp together. The pads launch the clips of the
  current scene: unlit = no clip, yellow = clip available, green = playing,
  red = recording; flashing colours mean the command is queued for the next
  bar.
- In Session view the transport is redefined: **Rewind = previous scene,
  Fast-Forward = next scene, Loop = play selected scene**.
- This extension maps the same behaviour onto Bitwig: the drum pads launch
  clips and the scene navigation works in the Mix panel (see
  [CONTROLS.md](CONTROLS.md)).

## Factory templates

| No. | Template | Hardware name |
| --- | --- | --- |
| 1 | Basic MIDI (avoids commonly used CCs) | `BascMIDI` |
| 2 | Commonly used MIDI CCs | `UsefulCC` |
| 3 | General MIDI mixer (faders volume, encoders pan, per channel) | `GM Mixer` |
| 4 | Ableton Live / Live Lite | `Live` |
| 5 | Propellerhead Reason | `Reason` |
| 6 | Apple GarageBand | `GarageBd` |
| 7 | Apple MainStage | `MainStge` |
| 8 | Novation Station series | `NovaStat` |
| 9 | Novation UltraNova | `UltrNova` |
| 10 | Novation Nova / Supernova | `SupaNova` |
| 11 | Native Instruments Kontakt | `Kontakt` |
| 12 | Native Instruments FM8 | `FM 8` |
| 13 | Native Instruments B4 Organ | `B4 Organ` |
| 14 | Native Instruments Massive | `Massive` |
| 15-20 | Blank user templates | `Blank` |

This extension writes its template into RAM with SysEx; the user stores it into
one of the blank templates (15-20) with the Save button.

## SysEx protocol

All messages use the Novation/Focusrite header `F0 00 20 29` and terminate with
`F7`. The commands below omit header and trailer.

| Command | Function |
| --- | --- |
| `00 70` | Get firmware version; the device replies with boot and main version |
| `67 06 x y z` | Device inquiry and zone control mode: x = faders, y = knobs, z = pads; the device replies with its device ID |
| `67 05 zone offset 00 <labels>` | Write persistent control labels; zone 0 = faders, 1 = buttons, 2 = knobs; eight characters per control, up to 72 bytes (9 controls), space padded |
| `67 08 <text>` | Write the eight-character text area of the display; longer text scrolls |
| `67 09 <3 chars>` | Write the three-character numeric area of the display |
| `43 00 00 <data>` | Write template data into the RAM of the current template bank (used by `Template.java`) |

### Zone modes

`67 06 x y z` selects how the three zones behave:

- **Mode A** – plugin/automap mode disabled, plain template MIDI
- **Mode B** – plugin mode without labels; controls send their template CCs
- **Mode C** – plugin mode with labels; controls send their CCs and the
  hardware displays the stored label and value (`No Ctrl` when no label is
  stored)

From the main MIDI port, `67 06 01 01 01` is a special case: all zones run in
Mode B and the mod wheel is output on a fixed channel. Any other combination
puts the faders and knobs in Mode A or C and the pads in Mode A (template
notes) or Mode B (clip launch CCs). Enabling any zone on the main port
redirects all control messages to the main port.

This extension sends `67 06 01 01 01`, so it runs fully in **Mode B** and
writes the display itself with `67 08` (text) and `67 09` (numeric value). The
optional **Control labels** preference stores labels with `67 05`; they become
visible when the Impulse runs in label mode (Mode C).

### Value display

The Impulse stores a value per control (default 0); sending the control's CC
message updates the stored value that Mode C displays. This extension echoes
the current value back for the same purpose and additionally writes the numeric
display area with `67 09` while in Mode B.

## Where this is used in the code

- `Midi.java` – the CC mapping constants
- `Template.java` – the SysEx template written with `43 00 00`
- `SysexSend.java` – display text, numeric display and control labels
- `Preferences.java` – Debug logging and Control labels preferences
