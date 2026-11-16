# Implementační plán – Novation Impulse pro Bitwig

Pracovní seznam funkcionalit, které plánujeme do pluginu implementovat.
Doplňková dokumentace: [HARDWARE.md](HARDWARE.md) (hardware a SysEx),
[CONTROLS.md](CONTROLS.md) (mapování ovladačů), [README](../README.md) (instalace a build).

**Stav:** verze 1.3, branch `improvements`, změny zatím necommitnuté (čeká se na git identitu).

## Hotovo (v1.2)

- [x] Migrace na Bitwig Extension API 25 (Bitwig 6.x), oprava pádu při inicializaci
- [x] Track bank 8 stop × 2 sends × 1 scéna (jedna scéna = správné LED padů)
- [x] Clip launch pady s LED stavy + Shift + Launch = stop všech clipů
- [x] Displej: text 8 znaků (doplnění mezerami), číselná plocha 3 znaky (`67 09`), `Scene N`
- [x] Volitelné perzistentní popisky ovladačů (`67 05`) – preference **Control labels**
- [x] Dokumentace: `CONTROLS.md`, `HARDWARE.md`, `README.md`
- [x] Build helpery pro Windows (`build.cmd` / `build.ps1`), JUnit testy (8)

## Priorita 0 – rychlé úpravy (návrh pro vydání 1.3)

- [x] **LED transportu podle stavu Bitwigu** *(v1.3; pozor: LED zpětná vazba
  transportu není v dokumentaci hardware potvrzená – ověřit na hardware)*
  Tlačítka Play / Record / Loop rozsvěcovat podle `transport.isPlaying()`,
  `isArrangerRecordEnabled()` a `isArrangerLoopEnabled()`. Ověřit hodnoty LED na
  hardware (0 = off, jiná hodnota = on).
  *Soubory:* `observers/TransportLedObserver`, `MidiSend`.

- [x] **Automatické odeslání šablony při startu** *(v1.3, default ON)*
  Nová preference *Send template at startup* (on/off, default on) – ušetří
  klikání v nastavení po každém restartu Bitwigu.
  *Soubory:* `Preferences`, `NovationImpulseExtension`.

- [x] **Undo / Redo** *(v1.3)*
  Přidat shift varianty transport tlačítek: Shift + Rewind = Undo,
  Shift + Fast-Forward = Redo (`application.undo()` / `application.redo()`).
  Zobrazení `Undo` / `Redo` na displeji.

- [x] **Vymazání všech mute / solo** *(v1.3)*
  Shift + tlačítko Mute/Solo = vypnout všechny mute (v solo režimu všechny
  solo). Iterace přes track bank (8 viditelných stop + master), displej
  `ClrMutes` / `ClrSolos`.
  *Soubory:* `commands/mixer/ClearAllMutesSolosCommand`, `Tracks`.

- [x] **Zpráva „Ready“ po inicializaci** *(v1.3)*
  Po odpovědi na device inquiry zobrazit na displeji krátký stav (model +
  ready), aby bylo vidět, že extension proběhl v pořádku.
  *Soubory:* `SysexCallback`, `SysexSend`.

## Priorita 1 – střední rozsah

- [x] **Více sends (Send 3–6) a stránky mixéru** *(v1.3)*
  Rozšířit track bank na 6 sends; tlačítko Mixer cyklicky listuje stránkami
  Pan → Send 1–6, název stránky se zobrazí na displeji.
  *Soubory:* `Midi.SEND_COUNT`, `Tracks`, `EncoderMixerModePage`,
  `EncoderMixerCommand`.

- [x] **Kontrola master faderu na Impulse 61** *(v1.3, ověřeno)*
  Ověřit, že CC 8 ovládá master track; pokud ne, opravit mapování pro 49/61.
  V kódu: `FaderCommand` CC 8 → `Tracks.get(8)` = master track, otestováno na
  hardware. *(Pro Impulse 25 zvlášť `FaderCursorCommand` → cursor track.)*

- [x] **Data knob – přepínač presetů** *(v1.3, PopupBrowser, otestováno 16.11.2026)*
  V normálním stavu knob mění šablonu a MIDI neposílá (není v zónách).
  **Prog Change režim** (Shift + MIDI Chan, exit = Enter) přepne knob na
  posílání **Program Change na kanálu 1**. První implementace přes
  `Device.loadPreset()` **spadla** – staré preset API je od API 25 zcela
  uzamčené (ověřeno dekompilací: `checkCanUseOldPresetAPI` volá i
  `switchToNext/PreviousPreset`); na API 1 by šlo použít, ale to by uzamklo
  zbytek moderního API. BrowsingSession API je **celé deprecated**
  („Use PopupBrowser instead" – dle lokální oficiální dokumentace Bitwigu).
  Finální řešení = **moderní PopupBrowser** (vzor DrivenByMoss `BrowserImpl`):
  `host.createPopupBrowser()`, otevření přes
  `cursorDevice.replaceDeviceInsertionPoint().browse()` (dialog pro výběr
  presetů vybraného zařízení), content typ „Preset" se zajistí přes
  `selectedContentTypeIndex` (browser se otevře na indexu 0 = Devices, kód
  přepne na první tab obsahující „Preset" = *Bitwig Presets*).
  **Pohyb seznamu řídí `browser.selectNextFile()/selectPreviousFile()`**
  (nativní akce browseru). Ověřeno na hardware: hýbou viditelným
  zvýrazněním a **browser je sám zarazí na krajích seznamu** (žádný únik
  fokusu). Alternativy nefungují: API kurzor results sloupce
  (`cursorResult.selectNext`) hýbe jen „skrytým" výběrem, šipky
  (`application.arrowKeyDown/Up`) na krajích seznamu unikají mimo seznam
  (fokus skočí do vyhledávání/filtrů) a mimo browser hýbají stopami,
  `cursorResult.hasNext/hasPrevious` jsou v novém browseru mrtvé (vždy
  false), `bank.cursorIndex()` čte vždy -1 (ale `bank.itemCount()` a
  `isSelected()` sken položek jsou živé). Žádná seed šipka (není potřeba,
  odstranila drift +1). Otevírání: otočení knobu armuje **lepkavý
  `pendingOpen`** → ticker volá `browse()` max 1× za cyklus (`browseSentAt`),
  potvrzení dělá **exists() observer** (při open=true maže požadavek, při
  false resetuje cyklus); retry jen do timeoutu 1,5 s, max 3 pokusy
  (dvojité `browse()` během otevírání způsobovalo blik open/close/open –
  zachyceno v logu jako dvě `open` 267 ms od sebe). Kroků šetřeno
  `closing` příznakem (commit/cancel zavírá asynchronně, šipky v okně
  zavírání by sletěly do hlavního okna a hýbaly stopami). Delta z PC čísel
  (s wrapem, testy) se hromadí (±64) a vypouští 1 krok/tick (2 při backlogu
  ≥ 6); první PC po startu jen „naučí" pozici (sentinela -1 dala k+1 kroků).
  Nahraní přes `browser.commit()` **1,2 s** po zastavení knobu (kratší
  doba poblikávala při pomalém otáčení). Browser se otevírá přímo na
  nahraném presetu → žádný drift mezi sessions. **Audition (náhled) je
  během knob-scanu vypnutý** (každý krok jinak nahraje preset pro náhled →
  růst heapu Bitwig JVM → jednou OOM crash systému, error 1455 pagefile).
  Displej: název presetu event-driven přes `cursorResult.name()` observer
  (API hodnota zaostává 1–2 kroky, update hned po krocích ukazoval
  „předchozí" název). Ticker (50 ms) řídí open/move/commit, protože
  `scheduleTask` z MIDI callbacku se nikdy nevykoná. Otestováno kompletně
  16.11.2026: rychlé/pomalé otáčení, kraje seznamu, opakované první
  otočení bez bliku, paměť stabilní.
  *Soubory:* `Midi.CH1_PC`, `commands/DataKnobCommand`, `Tracks.getCursorDevice()`,
  test wrap logiky.

- [ ] **Metronom a uložení projektu** *(návrh 16.11.2026 – porovnání s oficiálními
  Nektar scripty LX61/iX49)*
  Shift + Play = `transport.toggleClick()` (metronom), Shift + Stop =
  `application.save()`. Displej `Click` / `Save`. U Nektaru je Shift+Play =
  click (u nás Shift+Stop = undo? ne – u nás máme Undo na Shift+Rewind, takže
  Shift+Play i Shift+Stop jsou volné).
  *Soubory:* `commands/transport/`, `Midi.TRANSPORT_PLAY/STOP`, `Transport`.

- [ ] **Navigace zařízení v plugin režimu** *(návrh – porovnání s Nektar LX61)*
  V plugin režimu jsou Rewind/Forward nečinné → namapovat
  `cursorDevice.selectPrevious()/selectNext()` (Nektar: Instrument mód,
  Rewind/Forward přepínají zařízení v chainu). Displej: název vybraného
  zařízení.
  *Soubory:* `commands/transport/`, `Tracks.getCursorDevice()`.

- [ ] **Fader soft takeover (pickup)** *(návrh – přenos Nektar logiky `L04F`)*
  Fader posílá hodnotu jen když je fyzická poloha blízko hodnotě v Bitwigu
  (Nektar: okno ±3, stavy muted/low/high/active, timeout 250 ms). Bez toho
  hodnota „skočí", když se fader liší od aktuální hodnoty (typicky po
  přepnutí stránky mixéru nebo banky stop – fadery nejsou motorizované,
  takže je nelze přepozicovat). Otestovat na hardware (může cítit „lepkavě").
  *Soubory:* `FaderCommand`, `FaderCursorCommand`, nový `FaderTakeover`.

- [ ] **Device on/off v plugin režimu** *(návrh – porovnání s Nektar LX61)*
  Tlačítka pod fadery (CC 9–16) v plugin režimu přepínají enabled state
  zařízení v chainu (`device.toggleEnabledState()`), Shift + tlačítko
  vybere zařízení v chainu. Nektar: XC_INST_K4TT0–7 (8 tlačítek = 8
  zařízení, Shift = selectDevice).
  *Soubory:* `commands/`, `Tracks`, `CursorDevice`.

## Priorita 2 – experimenty a větší změny

- [ ] **Režim C s popiskami na displeji**
  Hardware umí zobrazovat popisky a hodnoty automaticky (Mode C). Problém:
  pady musí zůstat v Mode B (clip launch). Řešení dle HARDWARE.md: fadery/konby
  Mode C přes hlavní port + pady Mode B přes control port (`MIDIOUT2/3`).
  Vyžaduje: deklarovat další MIDI out porty v `NovationImpulseExtensionDefinition`,
  testovat na hardware, připravit fallback (revert na Mode B).

- [ ] **Expression pedal / mod wheel – cílová volba**
  Preference pro cíl CC 11 (aktuálně jde do note input). Zvážit mapování na
  vybraný parametr; nejdřív zjistit, co Bitwig API nabízí (cursor controls).

- [ ] **Plná podpora Impulse 25 a 49**
  Projít mapování pro menší modely (1 fader, 0 tlačítek pod fadery), testy
  proti `ImpulseModel`.

- [ ] **Punch in / out (loop range)** *(návrh – Nektar LX61)*
  Shift varianty transportu pro nastavení začátku/konce loopu
  (`transport.getPosition().set(...)`, in/out dle Nektaru LX61). U nás jsou
  Shift+Loop/Rewind/Forward volné v mix režimu (scény jsou bez Shiftu).
  *Soubory:* `commands/transport/`, `Transport`.

- [ ] **Master ↔ cursor track fader** *(návrh – Nektar LX61)*
  Tlačítko Master přepíná, zda fader 9 řídí master track nebo vybranou stopu.
  *Soubory:* `FaderCommand`, `Midi.BUTTON_MASTER`.

- [ ] **Data knob – drobná vylepšení** *(poznámky z implementace 16.11.2026)*
  a) Preference *Preview při otáčení* – náhledy (audition) jsou teď natvrdo
  vypnuté kvůli paměti (OOM crash); volitelný přepínač, default off.
  b) Plug-in Presets tab pro VST zařízení – teď se vždy volá první tab
  obsahující „Preset" (= *Bitwig Presets*); u pluginů by byl správný
  *Plug-in Presets* (rozpoznat podle typu zařízení).
  c) Experiment: stisk data knobu v PC režimu jako commit – zjistit, zda
  press něco vysílá; „otoč → stiskni = nahrát" by bylo přesnější UX
  než časovač 1,2 s.

## Údržba a infrastruktura

- [ ] **Git commity** – rozdělit změny do logických commitů (build, opravy,
  refaktor, dokumentace, features). Čeká na `user.name` / `user.email`.
- [ ] **CI** – GitHub Actions workflow (JDK 21 + Maven, artefakt `.bwextension`),
  případně nahradit `circle.yml`.
- [ ] **Release 1.3** – tag, připojit `.bwextension` k release, changelog v README.
- [x] **Verze** – bump na 1.3 s první novou funkcí *(pom.xml už je 1.3)*.

## Testovací checklist (hardware)

Po každé implementaci projít. **Otestováno 16.11.2026 na Impulse 61, verze 1.3
(hardware + log bez chyb):**

1. [x] Restart Bitwigu – log bez chyb extensionu.
2. [x] Fader/encoder pohnou displejem: název + číselná hodnota.
3. [x] Preference **Control labels** zapnuta → popisky odeslány; šablona uložena na Impulse.
4. [x] Transport LED reflektují stav přehrávání.
5. [x] Clip launch: pady rozsvěcují podle stavu, Shift + Launch stopne všechny clipy.
6. [x] Scény přes Rewind/Forward/Loop v Mix panelu, `Scene N` na displeji.
7. [x] Stránkování banky (Shift + track tlačítka) zobrazí `[first-last]`.
8. [x] Mute/Solo LED v obou režimech, přepínání tlačítkem Mute/Solo,
   Shift + Mute/Solo = clear all (`ClrMutes` / `ClrSolos`).

## Otevřené otázky

- Mode C: stojí za riziko, nebo zůstat u Mode B + vlastní displej?
