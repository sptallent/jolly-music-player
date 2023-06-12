package sage.musicplayer.MainTab;

import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import sage.musicplayer.MainActivity;
import sage.musicplayer.Util.DBUtil.DatabaseHelper;
import sage.musicplayer.Util.JollyUtils;
import sage.musicplayer.Util.MusicUtil.EQPreset;
import sage.musicplayer.Util.MusicUtil.JollyEqualizer;
import sage.musicplayer.R;
import sage.musicplayer.Util.UIUtil.CircularSeekBar;
import sage.musicplayer.Util.UIUtil.VerticalSeekBar;


public class SettingsTab extends Fragment {

  private AppCompatCheckBox check_include_ringtones;
  private AppCompatCheckBox dynamic_eq_cb;
  private TextView settings_equalizer;
  private PopupWindow eqPopupWindow;
  private PopupWindow eqPopupSave;
  private LinearLayout eqBottom;
  private LinearLayout eqContainer;
  private Button eqCloseButton;
  private Button eqSaveButton;
  private JollyEqualizer eq;
  private BassBoost boost;
  private Virtualizer virtualizer;
  private AppCompatSpinner eqDropDown;
  private CircularSeekBar volumeSeek;
  private CircularSeekBar bassSeek;
  private TextView eqSaveInput;
  private Button eqFinalSaveButton;
  private SwitchCompat equalizerSwitch;
  private TextView textEqualizerOnOff;
  Typeface latoReg;
  ArrayList<VerticalSeekBar> seekBarList;
  SharedPreferences settings;
  SharedPreferences.Editor editor;
  ArrayList<EQPreset> presets;
  ArrayList<String> presetsName;
  EQPreset curPreset = null;
  View settingsView;
  MediaPlayer mediaPlayer;
  String prefThemeName;
  TextView showPrivacyPolicy;
  TextView privacyPolicyView;
  TextView themeDefault;
  TextView backgroundDefault;
  TextView re_scan_music;
  TextView reset_fingerprint_database;
  TextView settings_notification_text;
  SwitchCompat notification_switch;
  TextView notification_on_off;
  DatabaseHelper dbHelper;
  AppCompatCheckBox playback_open_now_playing;
  AppCompatCheckBox show_bubbles;
  AppCompatCheckBox theme_default_bg_change;
  TextView bgoptions_transparent_blur_text;
  SwitchCompat bgoptions_switch;
  TextView theme_text;
  SwitchCompat theme_switch;
  TextView sleep_timer_text;
  TextView sleep_timer_text_left;
  JollyUtils jollyUtils;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_settings_tab, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    settingsView = view;

    dbHelper = new DatabaseHelper(getContext());
    jollyUtils = new JollyUtils(getContext());

    settings = getContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE);
    editor = getContext().getSharedPreferences("jolly_prefs", Context.MODE_PRIVATE).edit();

    theme_default_bg_change = view.findViewById(R.id.theme_default_bg_change);
    boolean bg_change_status = settings.getBoolean("bg_change_status", false);
    theme_default_bg_change.setChecked(bg_change_status);
    theme_default_bg_change.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean("bg_change_status", isChecked);
        editor.commit();
      }
    });

    show_bubbles = view.findViewById(R.id.show_bubbles);
    boolean show_bubbles_status = settings.getBoolean("show_bubbles", true);
    show_bubbles.setChecked(show_bubbles_status);
    show_bubbles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean("show_bubbles", isChecked);
        editor.commit();
        Intent intent = new Intent("show_bubbles");
        jollyUtils.sendLocalBroadcast(intent);
      }
    });

    playback_open_now_playing = view.findViewById(R.id.playback_open_now_playing);
    boolean open_now_playing_status = settings.getBoolean("now_playing_status", false);
    playback_open_now_playing.setChecked(open_now_playing_status);
    playback_open_now_playing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean("now_playing_status", isChecked);
        editor.commit();
      }
    });

    settings_notification_text = view.findViewById(R.id.settings_notification_text);
    notification_on_off = view.findViewById(R.id.notification_on_off);
    notification_switch = view.findViewById(R.id.notification_switch);
    boolean notificationsStatus = settings.getBoolean("notification_status", true);
    if (notificationsStatus)
      notification_on_off.setText(R.string.on);
    else
      notification_on_off.setText(R.string.off);
    notification_switch.setChecked(notificationsStatus);
    notification_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          notification_on_off.setText(R.string.on);
        } else {
          notification_on_off.setText(R.string.off);
          NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
          notificationManager.cancelAll();
        }
        editor.putBoolean("notification_status", isChecked);
        editor.commit();
      }
    });

    re_scan_music = view.findViewById(R.id.re_scan_music);
    re_scan_music.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((MainActivity) getActivity()).requestReadStorage(false);
        Toast.makeText(getContext(), getString(R.string.re_scanning_music_library),
                Toast.LENGTH_SHORT).show();
      }
    });

    reset_fingerprint_database = view.findViewById(R.id.reset_fingerprint_database);
    reset_fingerprint_database.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dbHelper.delAllFingerprintAndMBID();
        ((MainActivity) getActivity()).requestReadStorage(false);
        Toast.makeText(getContext(), getString(R.string.clearing_fingerprint_database),
                Toast.LENGTH_SHORT).show();
      }
    });

    dynamic_eq_cb = view.findViewById(R.id.dynamic_eq_checkbox);
    dynamic_eq_cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        setDEQ(isChecked);
      }
    });

    showPrivacyPolicy = view.findViewById(R.id.settings_privacy_show);
    privacyPolicyView = view.findViewById(R.id.privacy_policy_view);
    showPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (privacyPolicyView.getVisibility() == View.GONE) {
          privacyPolicyView.setVisibility(View.VISIBLE);
          showPrivacyPolicy.setText(R.string.hide_privacy_policy);
        } else {
          privacyPolicyView.setVisibility(View.GONE);
          showPrivacyPolicy.setText(R.string.show_privacy_policy);
        }
      }
    });

    String theme_choice = settings.getString("theme", "lightTheme");
    theme_text = view.findViewById(R.id.theme_text);
    theme_switch = view.findViewById(R.id.theme_switch);
    if(theme_choice.equals("darkTheme")) {
      theme_text.setText(getString(R.string.dark_theme));
      theme_switch.setChecked(true);
    }else{
      theme_text.setText(getString(R.string.light_theme));
      theme_switch.setChecked(false);
    }
    theme_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
          theme_text.setText(getString(R.string.dark_theme));
          setThemeDefault("dark", true);
        }else{
          theme_text.setText(getString(R.string.light_theme));
          setThemeDefault("light", true);
        }
      }
    });

    check_include_ringtones = view.findViewById(R.id.check_include_ringtones);
    boolean checkIncludeRingtonesStatus = settings.getBoolean("include_ringtones", false);
    check_include_ringtones.setChecked(checkIncludeRingtonesStatus);
    check_include_ringtones.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor.putBoolean("include_ringtones", isChecked);
        editor.commit();
      }
    });

    boolean eq_enabled_status = settings.getBoolean("eq_enabled", true);
    if (!eq_enabled_status) {
      editor.putBoolean("eq_enabled", true);
      editor.commit();
    }

    textEqualizerOnOff = view.findViewById(R.id.equalizer_on_off);
    equalizerSwitch = view.findViewById(R.id.equalizer_switch);
    if (eq_enabled_status) {
      equalizerSwitch.setChecked(true);
      textEqualizerOnOff.setText(R.string.on);
    } else {
      equalizerSwitch.setChecked(false);
      textEqualizerOnOff.setText(R.string.off);
    }
    equalizerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
          try {
            eq.setEnabled(true);
            boost.setEnabled(true);
            virtualizer.setEnabled(true);
            textEqualizerOnOff.setText(R.string.on);
            editor.putBoolean("eq_enabled", true);
            editor.commit();
          } catch (IllegalStateException ex) {
            ex.printStackTrace();
          }
        } else {
          try {
            eq.setEnabled(false);
            boost.setEnabled(false);
            virtualizer.setEnabled(false);
            textEqualizerOnOff.setText(R.string.off);
            editor.putBoolean("eq_enabled", false);
            editor.commit();
          } catch (IllegalStateException ex) {
            ex.printStackTrace();
          }
        }
      }
    });

    boolean dynamic_eq_status = settings.getBoolean("deq", false);
    if (!dynamic_eq_status) {
      editor.putBoolean("deq", false);
      editor.commit();
    }
    dynamic_eq_cb.setChecked(dynamic_eq_status);

    boolean background_options = settings.getBoolean("bgOptions", false);
    editor.putBoolean("bgOptions", background_options);
    editor.commit();
    bgoptions_transparent_blur_text = view.findViewById(R.id.bgoptions_transparent_blur_text);
    bgoptions_switch = view.findViewById(R.id.bgoptions_switch);
    if(background_options)
      bgoptions_transparent_blur_text.setText(getString(R.string.transparent));
    else
      bgoptions_transparent_blur_text.setText(getString(R.string.blur));
    bgoptions_switch.setChecked(background_options);
    bgoptions_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
          bgoptions_transparent_blur_text.setText(getString(R.string.transparent));
          setBackgroundOptions(true);
        }else{
          bgoptions_transparent_blur_text.setText(getString(R.string.blur));
          setBackgroundOptions(false);
        }
      }
    });

    sleep_timer_text = view.findViewById(R.id.sleep_timer_text);
    sleep_timer_text_left = view.findViewById(R.id.sleep_time_left_text);
    sleep_timer_text.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                  public void onTimeSet(TimePicker vi, int hours, int minutes) {
                    String hour_string;
                    String minute_string;
                    if(hours < 10)
                      hour_string = "0" + hours;
                    else
                      hour_string = "" + hours;
                    if(minutes < 10)
                      minute_string = "0" + minutes;
                    else
                      minute_string = "" + minutes;
                    sleep_timer_text_left.setText(hour_string + ":" + minute_string);
                    long selectedTimeInMillis = jollyUtils.getTimeInMillis(hours, minutes);
                    startSleepTimer(selectedTimeInMillis);
                    //Toast.makeText(getContext(), getString(R.string.sleep_timer_set), Toast.LENGTH_SHORT).show();
                  }
                }, 0, 0, true);
        timePickerDialog.show();
      }
    });

    if (settings.getString("eq_preset_normal", null) == null) {
      EQPreset temp = new EQPreset();
      temp.setLoudness(0);
      temp.setBassStrength(0);
      temp.setName("normal");

      temp.setBandDb(0, "1700");
      temp.setBandDb(1, "1500");
      temp.setBandDb(2, "1500");
      temp.setBandDb(3, "1500");
      temp.setBandDb(4, "1700");

      Gson gson = new Gson();
      String jsonObj = gson.toJson(temp);
      editor.putString("eq_preset_" + temp.getName(), jsonObj);
      editor.commit();
    }

    if (settings.getString("eq_preset_flat", null) == null) {
      EQPreset temp = new EQPreset();
      temp.setLoudness(0);
      temp.setBassStrength(0);
      temp.setName("flat");

      temp.setBandDb(0, "1500");
      temp.setBandDb(1, "1500");
      temp.setBandDb(2, "1500");
      temp.setBandDb(3, "1500");
      temp.setBandDb(4, "1500");

      Gson gson = new Gson();
      String jsonObj = gson.toJson(temp);
      editor.putString("eq_preset_" + temp.getName(), jsonObj);
      editor.commit();
    }

    if (settings.getString("eq_preset_rock", null) == null) {
      EQPreset temp = new EQPreset();
      temp.setLoudness(0);
      temp.setBassStrength(0);
      temp.setName("rock");

      temp.setBandDb(0, "1800");
      temp.setBandDb(1, "1800");
      temp.setBandDb(2, "1400");
      temp.setBandDb(3, "1400");
      temp.setBandDb(4, "1500");

      Gson gson = new Gson();
      String jsonObj = gson.toJson(temp);
      editor.putString("eq_preset_" + temp.getName(), jsonObj);
      editor.commit();
    }

    presets = new ArrayList<>();
    presetsName = new ArrayList<>();

    latoReg = ResourcesCompat.getFont(getContext(), R.font.latoregular);
    settings_equalizer = view.findViewById(R.id.settings_equalizer);
    settings_equalizer.setOnClickListener(settings_equalizer_onClick);

    LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View popupView = inflater.inflate(R.layout.eq_popup, null);
    int width = LinearLayout.LayoutParams.MATCH_PARENT;
    int height = LinearLayout.LayoutParams.MATCH_PARENT;
    eqPopupWindow = new PopupWindow(popupView, width, height, true);

    View saveView = inflater.inflate(R.layout.eq_save_popup, null);
    int save_width = LinearLayout.LayoutParams.MATCH_PARENT;
    int save_height = LinearLayout.LayoutParams.WRAP_CONTENT;
    eqPopupSave = new PopupWindow(saveView, save_width, save_height, true);
  }

  View.OnClickListener settings_equalizer_onClick = v -> {
    if (settings.getBoolean("eq_enabled", true)) {
      eqPopupWindow.showAtLocation(settingsView, Gravity.CENTER, 0, 0);

      eqBottom = eqPopupWindow.getContentView().findViewById(R.id.eq_bottom);
      eqContainer = eqPopupWindow.getContentView().findViewById(R.id.eq_container);
      eqCloseButton = eqPopupWindow.getContentView().findViewById(R.id.eq_close_button);
      eqSaveButton = eqPopupWindow.getContentView().findViewById(R.id.eq_save_button);
      eqDropDown = eqPopupWindow.getContentView().findViewById(R.id.eq_drop_down);
      volumeSeek = eqPopupWindow.getContentView().findViewById(R.id.volumeSeek);
      bassSeek = eqPopupWindow.getContentView().findViewById(R.id.bassSeek);

      presetsName = new ArrayList<>();
      presets = new ArrayList<>();
      Map<String, ?> allSettings = settings.getAll();
      for (Map.Entry<String, ?> entry : allSettings.entrySet()) {
        if (entry.getKey().contains("eq_preset_")) {
          if (entry.getValue() != null) {
            Gson gson = new Gson();
            EQPreset temp = gson.fromJson(entry.getValue().toString(), EQPreset.class);
            presets.add(temp);
            presetsName.add(temp.getName());
            eqDropDown.invalidate();
          }
        }
      }

      View.OnClickListener settings_eq_save_onClick = v2 -> {
        if (eq != null) {
          eqPopupSave.showAtLocation(settingsView, Gravity.BOTTOM, 0, 0);
          eqFinalSaveButton = eqPopupSave.getContentView().findViewById(R.id.eq_final_save_button);
          eqSaveInput = eqPopupSave.getContentView().findViewById(R.id.eq_name_input);

          eqFinalSaveButton.setOnClickListener(v3 -> {
            ArrayList<String> tempList = new ArrayList<>();
            for (int s = 0; s < seekBarList.size(); s++) {
              tempList.add(String.valueOf(seekBarList.get(s).getProgress()));
            }
            savePreset(eqSaveInput.getText().toString(), bassSeek.getProgress(), volumeSeek.getProgress(), tempList);
            eqPopupSave.dismiss();
          });
        }
      };

      View.OnClickListener settings_eq_close_onClick = v4 -> {
        eqPopupWindow.dismiss();
      };

      eqCloseButton.setOnClickListener(settings_eq_close_onClick);
      eqSaveButton.setOnClickListener(settings_eq_save_onClick);

      eqContainer.removeAllViews();
      if (eq != null) {
        try {
          eq.setEnabled(true);
          boost.setEnabled(true);
          virtualizer.setEnabled(true);
          bassSeek.setLockEnabled(false);
          volumeSeek.setLockEnabled(false);
          eqDropDown.setEnabled(true);

          ArrayAdapter<String> presetAdapter = new ArrayAdapter<>(eqPopupWindow.getContentView().getContext(), R.layout.eq_drop_down_item, presetsName);
          eqDropDown.setAdapter(presetAdapter);

          eqDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
              EQPreset tempPreset = presets.get(i);
              updateEqualizer(tempPreset, true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
          });
          buildEqualizer();
        }catch(IllegalStateException ex) {
          ex.printStackTrace();
        }
      } else {
        TextView tv = new TextView(getContext());
        tv.setText(R.string.not_found);
        tv.setTypeface(latoReg);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(R.attr.primaryTextColor, typedValue, true);
        @ColorInt int color = typedValue.data;

        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER);
        eqContainer.addView(tv);
        bassSeek.setLockEnabled(true);
        volumeSeek.setLockEnabled(true);
        eqDropDown.setEnabled(false);
      }
    } else {
      Toast.makeText(getContext(), R.string.eq_disabled, Toast.LENGTH_SHORT).show();
    }
  };

  public void setEq(JollyEqualizer e, BassBoost b, Virtualizer v) {
    eq = e;
    boost = b;
    virtualizer = v;
  }

  public void buildEqualizer() {
    final short minEqLevel = eq.getBandLevelRange()[0];
    final short maxEqLevel = eq.getBandLevelRange()[1];
    short bands = eq.getNumberOfBands();
    seekBarList = new ArrayList<>();

    if (settings.getString("current_eq_preset", null) == null) {
      Gson gson = new Gson();
      String flatJson = settings.getString("eq_preset_flat", null);
      curPreset = gson.fromJson(flatJson, EQPreset.class);
      editor.putString("current_eq_preset", flatJson);
      editor.commit();
    } else {
      if (curPreset == null) {
        Gson gson = new Gson();
        curPreset = gson.fromJson(settings.getString("current_eq_preset", null), EQPreset.class);
      }
    }

    TypedValue typedValue = new TypedValue();
    Resources.Theme theme = getContext().getTheme();
    theme.resolveAttribute(R.attr.primaryTextColor, typedValue, true);
    @ColorInt int color = typedValue.data;

    for (short s = 0; s < bands; s++) {

      final short band = s;

      LinearLayout col = new LinearLayout(getContext());
      col.setOrientation(LinearLayout.VERTICAL);
      col.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

      col.setPadding(50, 10, 0, 10);

      TextView freqTv = new TextView(getContext());
      freqTv.setTypeface(latoReg);
      freqTv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
      freqTv.setGravity(Gravity.CENTER_HORIZONTAL);
      freqTv.setText(numberFormat((eq.getCenterFreq(band) / 1000)) + " " + getResources().getString(R.string.hz));
      freqTv.setTextColor(color);
      freqTv.setSingleLine(true);

      TextView maxDbTv = new TextView(getContext());
      maxDbTv.setTypeface(latoReg);
      maxDbTv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      maxDbTv.setText((eq.getBandLevel(band) / 100) + " " + getResources().getString(R.string.db));
      maxDbTv.setTextColor(color);
      maxDbTv.setSingleLine(true);
      maxDbTv.setGravity(Gravity.CENTER_HORIZONTAL);

      FrameLayout seekFrame = new FrameLayout(getContext());
      seekFrame.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

      VerticalSeekBar seekBar = new VerticalSeekBar(getContext());
      seekBar.setElevation(10.0f);
      seekBar.setThumb(getResources().getDrawable(R.drawable.thumb));
      seekBar.setProgressDrawable(getResources().getDrawable(R.drawable.eq_seek_bar_bg));
      /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        seekBar.setTickMark(getResources().getDrawable(R.drawable.eq_seek_bar_tick));
      }*/
      seekBar.setMax(maxEqLevel - minEqLevel);
      seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
          eq.setBandLevel(band, (short) (i + minEqLevel));
          maxDbTv.setText((eq.getBandLevel(band) / 100) + " " + getResources().getString(R.string.db));
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
      });
      seekBar.setProgress(eq.getBandLevel(s));
      seekBarList.add(seekBar);
      seekFrame.addView(seekBar);
      seekFrame.setMinimumHeight(500);

      col.addView(maxDbTv);
      col.addView(seekFrame);
      col.addView(freqTv);
      //col.addView(minDbTv);

      eqContainer.addView(col);

      bassSeek.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
          boost.setEnabled(true);
          boost.setStrength((short) (progress * 10));
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }
      });

      volumeSeek.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
        @Override
        public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
          virtualizer.setEnabled(true);
          virtualizer.setStrength((short) (progress * 10));
        }

        @Override
        public void onStopTrackingTouch(CircularSeekBar seekBar) {

        }

        @Override
        public void onStartTrackingTouch(CircularSeekBar seekBar) {

        }
      });
    }

    int pos = 0;
    for (int i = 0; i < presetsName.size(); i++) {
      if (curPreset.getName().equals(presetsName.get(i)))
        pos = i;
    }
    eqDropDown.setSelection(pos);
    updateEqualizer(curPreset, true);
  }

  public void updateEqualizer(EQPreset eqPreset, boolean changeCur) {
    if (eq != null) {
      curPreset = eqPreset;
      Gson gson = new Gson();
      String curJson = gson.toJson(curPreset, EQPreset.class);
      if (changeCur) {
        editor.putString("current_eq_preset", curJson);
        editor.commit();
      }

      short bands = eq.getNumberOfBands();
      for (short s = 0; s < bands; s++)
        seekBarList.get(s).setProgress(eqPreset.getBandDb(s));
      bassSeek.setProgress(eqPreset.getBassStrength());
      volumeSeek.setProgress(eqPreset.getLoudness());
    }
  }

  public void savePreset(String name, int bstrength, int lstrength, ArrayList<String> bandList) {
    EQPreset newPreset = new EQPreset();
    newPreset.setName(name);
    newPreset.setBassStrength(bstrength);
    newPreset.setLoudness(lstrength);
    for (int i = 0; i < bandList.size(); i++) {
      newPreset.setBandDb(i, bandList.get(i));
    }

    Gson gson = new Gson();
    String jsonObj = gson.toJson(newPreset);
    editor.putString("eq_preset_" + name, jsonObj);
    editor.commit();
    presets.add(newPreset);
    presetsName.add(name);
    eqDropDown.invalidate();
    eqDropDown.setSelection(presetsName.size() - 1, true);

    curPreset = newPreset;
    updateEqualizer(curPreset, true);
  }

  public void deletePreset(EQPreset preset) {
    editor.remove("eq_preset_" + preset.getName());
    editor.commit();
    for (int i = 0; i < presetsName.size(); i++) {
      if (preset.getName().equals(presetsName.get(i))) {
        presetsName.remove(i);
        presets.remove(i);
      }
    }
    eqDropDown.invalidate();
  }

  public String numberFormat(Number number) {
    char[] suffix = {' ', 'k', 'M', 'B', 'T', 'P', 'E'};
    long numValue = number.longValue();
    int value = (int) Math.floor(Math.log10(numValue));
    int base = value / 3;
    if (value >= 3 && base < suffix.length) {
      return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
    } else {
      return new DecimalFormat("#,##0").format(numValue);
    }
  }

  public void setDEQ(boolean checked) {
    editor.putBoolean("deq", checked);
    editor.commit();
  }

  public void setDEQPreset(String genre) {
    String[] genres;
    if (genre != null) {
      if (!genre.contains(",")) {
        genres = new String[1];
        genres[0] = genre;
      } else {
        genres = genre.split(",");
      }
      if (genres.length > 0) {
        boolean foundPreset = false;
        for (EQPreset preset : presets) {
          for (String g : genres) {
            if (g.equalsIgnoreCase(preset.getName())) {
              updateEqualizer(preset, false);
              foundPreset = true;
              break;
            }else{
              setDefaultEQ();
            }
          }
          if (foundPreset)
            break;
        }
        if(!foundPreset)
          setDefaultEQ();
      } else {
        setDefaultEQ();
      }
    }else{
      setDefaultEQ();
    }
  }

  public void setDefaultEQ() {
    Gson gson = new Gson();
    EQPreset p = (EQPreset)gson.fromJson(settings.getString("current_eq_preset", null), EQPreset.class);
    updateEqualizer(p, true);
  }

  public void setThemeDefault(String theme, boolean recreate) {
    int themeId = R.style.ThemeDark;
    if(theme.equals("light")) {
      //getContext().setTheme(R.style.ThemeLight);
      themeId = R.style.ThemeLight;
      editor.putString("theme", "lightTheme");
      editor.commit();
    }else if(theme.equals("dark")) {
      //getContext().setTheme(R.style.ThemeDark);
      editor.putString("theme", "darkTheme");
      editor.commit();
    }
    if(recreate)
      getActivity().recreate();
  }


  public void setBackgroundOptions(boolean transparent){//true transparent, false blur
    editor.putBoolean("bgOptions", transparent);
    editor.commit();
    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent("updateBgOptions"));
  }

  private void startSleepTimer(long selectedTimeInMillis) {
    // Create a new Handler and Runnable to perform the sleep timer action
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
      public void run() {
        Intent sleepTimerStop = new Intent("sleepTimerStop");
        jollyUtils.sendLocalBroadcast(sleepTimerStop);
      }
    };

    // Post the Runnable to the Handler with a delay equal to the selected time
    long delayInMillis = selectedTimeInMillis - System.currentTimeMillis();
    handler.postDelayed(runnable, delayInMillis);
  }

}
