package com.example.ui

import java.util.Locale

object I18n {
    const val LANG_AUTO = "auto"
    const val LANG_ZH_CN = "zh-CN"
    const val LANG_ZH_TW = "zh-TW"
    const val LANG_EN = "en"
    const val LANG_KO = "ko"
    const val LANG_JA = "ja"

    // Multi-language translation map
    private val translations = mapOf(
        LANG_ZH_CN to mapOf(
            "app_title" to "心情日志",
            "app_sub" to "关联时间，支持一日记录，自定义活动类别",
            "tab_record" to "记心情",
            "tab_calendar" to "今日历",
            "tab_insights" to "析数据",
            
            // Mood ratings
            "rating_1" to "糟糕透顶",
            "rating_2" to "有些难过",
            "rating_3" to "平静如水",
            "rating_4" to "心情不错",
            "rating_5" to "精彩极了",
            
            // Record section
            "how_are_you" to "此时此刻你觉得如何呢？",
            "write_feeling" to "写下此时的心情与感受...",
            "activities_happened" to "发生的活动...",
            "btn_save_record" to "保存今日心情记录",
            "btn_update_record" to "修改并保存此记录",
            "btn_clear_form" to "清除当前表单",
            "btn_delete_record" to "删除此心情记录",
            "current_date_log" to "当前日期记录",
            "mood_history_title" to "历史记录列表",
            "saved_at" to "记录于",
            "no_logs_on_date" to "选定日期暂无任何心情日志，点击上方评分开始记一笔吧！✨",
            "toast_add_success" to "心情已经记录成功！✨",
            "toast_update_success" to "心情记录修改成功！✨",
            "toast_delete_success" to "记录已成功删除",
            "choose_time" to "选择记录时间",

            // Calendar section
            "cal_heatmap_subtitle" to "过去 26 周每日心情分布热力图",
            "cal_legend_less" to "低评分",
            "cal_legend_more" to "高评分",
            "cal_detail_title" to "心情详细日志",
            "streak_display" to "已连续记录 %d 天！🔥",

            // Insights section
            "ins_all_time_title" to "最近7天心情走势",
            "ins_distribution_title" to "心情分布",
            "ins_top_activities" to "高频活动统计",
            "ins_card_empty" to "暂无足够数据生成分析图表，记录多几天再来看看吧！✨",
            
            // Backup settings
            "backup_title" to "系统数据备份与还原",
            "backup_desc" to "您可以将心情数据导出为本地备份 JSON 文件，或从备份文件中还原日志。数据完全保存在本地，保障您的隐私安全。",
            "btn_export" to "导出备份",
            "btn_import" to "导入还原",
            "toast_export_success" to "数据已成功备份导出！",
            "toast_import_success" to "备份导入并还原成功！",
            "toast_import_fail" to "导入失败，JSON 格式可能不匹配",
            "export_filename_hint" to "心情日志备份",

            // Activity tag manager
            "tag_manager_title" to "活动图标与类型管理",
            "tag_manager_desc" to "点击已存在的活动来进行编辑修改，或进行单独删除。图标输入栏推荐填入表情符号（Emoji）。",
            "tag_panel_edit" to "✏️ 修改当前活动",
            "tag_panel_add" to "➕ 自定义一个新活动",
            "tag_label_emoji" to "图标",
            "tag_label_name" to "活动名",
            "tag_placeholder_emoji" to "🍲",
            "tag_placeholder_name" to "例如 骑行",
            "btn_tag_delete" to "🗑️ 直接删除",
            "btn_tag_cancel" to "取消",
            "btn_tag_save" to "保存修改",
            "btn_tag_add" to "添加并建立",
            "toast_tag_delete_success" to "已成功删除活动: %s",
            "toast_tag_update_success" to "更新活动成功！✨",
            "toast_tag_add_success" to "添加活动成功！✨",
            "toast_tag_error" to "操作失败，重名或字段为空",

            // Theme Settings
            "theme_title" to "应用界面主题色设置",
            "theme_mode_auto" to "跟随系统 (Auto)",
            "theme_mode_light" to "清新亮色 (Light)",
            "theme_mode_dark" to "护眼暗色 (Dark)",

            // Language Settings
            "lang_settings_title" to "应用多语言切换 settings",
            "lang_auto" to "系统默认 (Default)",
            "lang_zh_cn" to "简体中文 (ZH)",
            "lang_zh_tw" to "繁體中文 (ZH-TW)",
            "lang_en" to "English (EN)",
            "lang_ko" to "한국어 (KO)",
            "lang_ja" to "日本語 (JA)",

            // Default mood advice (insights)
            "advice_excellent" to "继续保持乐观！",
            "advice_good" to "今天也是美好的一天～",
            "advice_neutral" to "平平淡淡才是真。",
            "advice_bad" to "稍微有些烦躁...",
            "advice_terrible" to "需要一些温暖和关怀...",

            "ins_total_records" to "总记录条数",
            "ins_average_rating" to "平均心情得分",
            "ins_happy_rate" to "心情优良率"
        ),

        LANG_ZH_TW to mapOf(
            "app_title" to "心情日誌",
            "app_sub" to "關聯時間，支持一日記錄，自定義活動類別",
            "tab_record" to "記心情",
            "tab_calendar" to "今日曆",
            "tab_insights" to "析數據",
            
            "rating_1" to "糟糕透頂",
            "rating_2" to "有些難過",
            "rating_3" to "平靜如水",
            "rating_4" to "心情不錯",
            "rating_5" to "精彩極了",
            
            "how_are_you" to "此時此刻你覺得如何呢？",
            "write_feeling" to "寫下此時的心情與感受...",
            "activities_happened" to "發生的活動...",
            "btn_save_record" to "保存今日心情記錄",
            "btn_update_record" to "修改並保存此記錄",
            "btn_clear_form" to "清除當前表單",
            "btn_delete_record" to "刪除此心情記錄",
            "current_date_log" to "當前日期記錄",
            "mood_history_title" to "歷史記錄列表",
            "saved_at" to "記錄於",
            "no_logs_on_date" to "選定日期暫無任何心情日誌，點擊上方評分開始記一筆吧！✨",
            "toast_add_success" to "心情已經記錄成功！✨",
            "toast_update_success" to "心情記錄修改成功！✨",
            "toast_delete_success" to "記錄已成功刪除",
            "choose_time" to "選擇記錄時間",

            "cal_heatmap_subtitle" to "過去 26 週每日心情分布熱力圖",
            "cal_legend_less" to "低評分",
            "cal_legend_more" to "高評分",
            "cal_detail_title" to "心情詳細日誌",
            "streak_display" to "已連續記錄 %d 天！🔥",

            "ins_all_time_title" to "最近7天心情走勢",
            "ins_distribution_title" to "心情分布",
            "ins_top_activities" to "高頻活動統計",
            "ins_card_empty" to "暫無足夠數據生成分析圖表，記錄多幾天再来看看吧！✨",
            
            "backup_title" to "系統數據備份與還原",
            "backup_desc" to "您可以將心情數據導出為本地備份 JSON 文件，或從備份文件中還原日誌。數據完全保存在本地，保障您的隱私安全。",
            "btn_export" to "導出備份",
            "btn_import" to "導入還原",
            "toast_export_success" to "數據已成功備份導出！",
            "toast_import_success" to "備份導入並還原成功！",
            "toast_import_fail" to "導入失敗，JSON 格式可能不匹配",
            "export_filename_hint" to "心情日誌備份",

            "tag_manager_title" to "活動圖標與類型管理",
            "tag_manager_desc" to "點擊已存在的活動來進行編輯修改，或進行單獨刪除。圖標輸入欄推薦填入表情符號（Emoji）。",
            "tag_panel_edit" to "✏️ 修改當前活動",
            "tag_panel_add" to "➕ 自定義一個新活動",
            "tag_label_emoji" to "圖標",
            "tag_label_name" to "活動名",
            "tag_placeholder_emoji" to "🍲",
            "tag_placeholder_name" to "例如 騎行",
            "btn_tag_delete" to "🗑️ 直接刪除",
            "btn_tag_cancel" to "取消",
            "btn_tag_save" to "保存修改",
            "btn_tag_add" to "添加並建立",
            "toast_tag_delete_success" to "已成功刪除活動: %s",
            "toast_tag_update_success" to "更新活動成功！✨",
            "toast_tag_add_success" to "添加活動成功！✨",
            "toast_tag_error" to "操作失敗，重名或欄位為空",

            "theme_title" to "應用界面主題色設置",
            "theme_mode_auto" to "跟隨系統 (Auto)",
            "theme_mode_light" to "清新亮色 (Light)",
            "theme_mode_dark" to "護眼暗色 (Dark)",

            "lang_settings_title" to "應用多語言切換 settings",
            "lang_auto" to "系統默認 (Default)",
            "lang_zh_cn" to "簡體中文 (ZH)",
            "lang_zh_tw" to "繁體中文 (ZH-TW)",
            "lang_en" to "English (EN)",
            "lang_ko" to "한국어 (KO)",
            "lang_ja" to "日本語 (JA)",

            "advice_excellent" to "繼續保持樂觀！",
            "advice_good" to "今天也是美好的一天～",
            "advice_neutral" to "平平淡淡才是真。",
            "advice_bad" to "稍微有些煩躁...",
            "advice_terrible" to "需要一些溫暖和關懷...",

            "ins_total_records" to "總記錄條數",
            "ins_average_rating" to "平均心情得分",
            "ins_happy_rate" to "心情優良率"
        ),

        LANG_EN to mapOf(
            "app_title" to "Mood Tracker",
            "app_sub" to "Time-linked, daily multiple logs & custom categories.",
            "tab_record" to "Record",
            "tab_calendar" to "Calendar",
            "tab_insights" to "Insights",
            
            "rating_1" to "Terrible",
            "rating_2" to "Sad",
            "rating_3" to "Neutral",
            "rating_4" to "Good",
            "rating_5" to "Awesome",
            
            "how_are_you" to "How are you feeling right now?",
            "write_feeling" to "Write down your thoughts & feelings...",
            "activities_happened" to "Activities...",
            "btn_save_record" to "Save Today's Mood Log",
            "btn_update_record" to "Modify and Save Mood Log",
            "btn_clear_form" to "Clear Current Form",
            "btn_delete_record" to "Delete This Mood Log",
            "current_date_log" to "Current Date Log",
            "mood_history_title" to "Mood History",
            "saved_at" to "Registered at",
            "no_logs_on_date" to "No mood logs for this date. Tap ratings above to begin logging! ✨",
            "toast_add_success" to "Mood registered successfully! ✨",
            "toast_update_success" to "Mood updated successfully! ✨",
            "toast_delete_success" to "Log deleted successfully",
            "choose_time" to "Choose register time",

            "cal_heatmap_subtitle" to "Daily Mood Heatmap of the past 26 weeks",
            "cal_legend_less" to "Less",
            "cal_legend_more" to "More",
            "cal_detail_title" to "Mood Details",
            "streak_display" to "%d days streak logged! 🔥",

            "ins_all_time_title" to "Recent 7 Days Trend",
            "ins_distribution_title" to "Mood Distribution",
            "ins_top_activities" to "Top Activities",
            "ins_card_empty" to "Not enough data to analyze. Keep logging for a few days! ✨",
            
            "backup_title" to "Data Backup & Restore",
            "backup_desc" to "You can export all mood entries to a local JSON file or import from a previous backup. All your personal data is saved locally on your device.",
            "btn_export" to "Export Backup",
            "btn_import" to "Import Backup",
            "toast_export_success" to "Backup exported successfully!",
            "toast_import_success" to "Backup imported successfully!",
            "toast_import_fail" to "Import failed. Invalid JSON structure",
            "export_filename_hint" to "Mood_Logs_Backup",

            "tag_manager_title" to "Activities & Tags Manager",
            "tag_manager_desc" to "Tap any activity tag to edit or delete it individually. We recommend inserting emoji characters as icons.",
            "tag_panel_edit" to "✏️ Modify Selected Activity",
            "tag_panel_add" to "➕ Customize New Activity",
            "tag_label_emoji" to "Icon",
            "tag_label_name" to "Activity Name",
            "tag_placeholder_emoji" to "🍲",
            "tag_placeholder_name" to "e.g. Cycling",
            "btn_tag_delete" to "🗑️ Delete",
            "btn_tag_cancel" to "Cancel",
            "btn_tag_save" to "Save Changes",
            "btn_tag_add" to "Add and Save",
            "toast_tag_delete_success" to "Deleted activity tag: %s",
            "toast_tag_update_success" to "Activity updated! ✨",
            "toast_tag_add_success" to "Activity created successfully! ✨",
            "toast_tag_error" to "Failed. Name duplicate or values blank",

            "theme_title" to "Theme Customization",
            "theme_mode_auto" to "Follow System (Auto)",
            "theme_mode_light" to "Fresh Mint (Light)",
            "theme_mode_dark" to "Modern Obsidian (Dark)",

            "lang_settings_title" to "Languages settings",
            "lang_auto" to "System Locale (Default)",
            "lang_zh_cn" to "简体中文 (ZH)",
            "lang_zh_tw" to "繁體中文 (ZH-TW)",
            "lang_en" to "English (EN)",
            "lang_ko" to "한국어 (KO)",
            "lang_ja" to "日本語 (JA)",

            "advice_excellent" to "Keep up the excellent mood!",
            "advice_good" to "It's a beautiful day~",
            "advice_neutral" to "Peaceful and serene.",
            "advice_bad" to "A bit moody and tense...",
            "advice_terrible" to "Take some warmth and deep breaths...",

            "ins_total_records" to "Total Records",
            "ins_average_rating" to "Avg Mood Rating",
            "ins_happy_rate" to "Ratio of Delightness"
        ),

        LANG_KO to mapOf(
            "app_title" to "기분 일기",
            "app_sub" to "시간과 연동, 하루 다중 기록 및 사용자 활동 카테고리 설정",
            "tab_record" to "기록하기",
            "tab_calendar" to "캘린더",
            "tab_insights" to "통계 분석",
            
            "rating_1" to "매우 나쁨",
            "rating_2" to "조금 우울",
            "rating_3" to "평온함",
            "rating_4" to "기분 좋음",
            "rating_5" to "최고의 날",
            
            "how_are_you" to "지금 이 순간, 당신의 기분은 어떤가요?",
            "write_feeling" to "지금 느끼는 생각과 일기를 적어보세요...",
            "activities_happened" to "수행한 활동...",
            "btn_save_record" to "오늘의 기분 저장",
            "btn_update_record" to "기록 수정 후 저장",
            "btn_clear_form" to "입력창 초기화",
            "btn_delete_record" to "기분 기록 삭제",
            "current_date_log" to "선택한 날짜의 기록",
            "mood_history_title" to "내 기록 타임라인",
            "saved_at" to "기록 시각",
            "no_logs_on_date" to "해당 날짜에 기분 일기가 없습니다. 위의 별점을 탭해 첫 기록을 남겨보세요! ✨",
            "toast_add_success" to "기분 일기가 성공적으로 저장되었습니다! ✨",
            "toast_update_success" to "기분 일기가 수정되었습니다! ✨",
            "toast_delete_success" to "기록이 정상적으로 삭제되었습니다.",
            "choose_time" to "기록할 시간 선택",

            "cal_heatmap_subtitle" to "지난 26주 동안의 일일 기분 흐름도",
            "cal_legend_less" to "낮은 기분",
            "cal_legend_more" to "높은 기분",
            "cal_detail_title" to "상세 기록 정보",
            "streak_display" to "%d일 연속 기록 중! 🔥",

            "ins_all_time_title" to "최근 7일 기분 동향",
            "ins_distribution_title" to "기분 통계 비율",
            "ins_top_activities" to "인기 활동 카테고리",
            "ins_card_empty" to "충분한 분석 데이터가 존재하지 않습니다. 며칠 간 더 기록해보세요! ✨",
            
            "backup_title" to "데이터 백업 및 복원",
            "backup_desc" to "모든 기분 데이터를 로컬 JSON 백업 파일로 내보내거나, 백업 파일을 가볍게 불러와 복원할 수 있습니다. 개인 정보 보호를 위해 데이터는 전적으로 기기 내에 안전하게 보관됩니다.",
            "btn_export" to "데이터 백업",
            "btn_import" to "백업 복원",
            "toast_export_success" to "데이터 백업 완료!",
            "toast_import_success" to "데이터 백업 복원 성공!",
            "toast_import_fail" to "복원에 실패했습니다. 올바르지 않은 JSON 파일입니다.",
            "export_filename_hint" to "기분일기_데이터_백업",

            "tag_manager_title" to "카테고리 및 활동 변경",
            "tag_manager_desc" to "기존 활동 태그를 탭하여 이름과 이모지를 편집하거나 삭제할 수 있습니다. 이모지 삽입을 적극 권장합니다.",
            "tag_panel_edit" to "✏️ 선택한 활동 수정",
            "tag_panel_add" to "➕ 새로운 활동 이름 생성",
            "tag_label_emoji" to "이모티콘",
            "tag_label_name" to "활동명",
            "tag_placeholder_emoji" to "🍲",
            "tag_placeholder_name" to "예시: 사이클링",
            "btn_tag_delete" to "🗑️ 삭제",
            "btn_tag_cancel" to "취소",
            "btn_tag_save" to "수정 완료",
            "btn_tag_add" to "추가 저장",
            "toast_tag_delete_success" to "다음 활동이 제거되었습니다: %s",
            "toast_tag_update_success" to "활동 태그가 수정되었습니다! ✨",
            "toast_tag_add_success" to "활동 태그가 성곡적으로 추가되었습니다! ✨",
            "toast_tag_error" to "실패. 중복된 이름 혹은 입력란 빈칸",

            "theme_title" to "테마 변경",
            "theme_mode_auto" to "시스템 설정 자동 따름 (Auto)",
            "theme_mode_light" to "싱그러운 민트 테마 (Light)",
            "theme_mode_dark" to "모던 옵시디언 테ma (Dark)",

            "lang_settings_title" to "언어 전환 설정",
            "lang_auto" to "시스템 언어 따름 (Default)",
            "lang_zh_cn" to "简体中文 (ZH)",
            "lang_zh_tw" to "繁體中文 (ZH-TW)",
            "lang_en" to "English (EN)",
            "lang_ko" to "한국어 (KO)",
            "lang_ja" to "日本語 (JA)",

            "advice_excellent" to "기분 좋은 텐션을 쭉 유지해보세요!",
            "advice_good" to "긍정적인 기운이 도는 멋진 하루입니다~",
            "advice_neutral" to "무난하고 편안함이 가득한 일상.",
            "advice_bad" to "조금 기분이 가라앉거나 예민할 수 있는 날...",
            "advice_terrible" to "스스로를 토닥이며 편하게 쉴 준비를 해봐요...",

            "ins_total_records" to "총 누적 일기수",
            "ins_average_rating" to "평균 기분 점수",
            "ins_happy_rate" to "긍정적 기분 확률"
        ),

        LANG_JA to mapOf(
            "app_title" to "感情日記",
            "app_sub" to "時間連携、一日の複数記録、カスタマイズされた活動カテゴリ対応",
            "tab_record" to "日記を書く",
            "tab_calendar" to "カレンダー",
            "tab_insights" to "データ分析",
            
            "rating_1" to "最悪",
            "rating_2" to "ちょっとブルー",
            "rating_3" to "穏やか",
            "rating_4" to "いいかんじ",
            "rating_5" to "最高の一日",
            
            "how_are_you" to "今この瞬間、あなたの感情・心地はいかがですか？",
            "write_feeling" to "今の感情や考えを記録してみましょう...",
            "activities_happened" to "行ったアクティビティ...",
            "btn_save_record" to "今日の気分を記録する",
            "btn_update_record" to "日記を修正して保存",
            "btn_clear_form" to "フォームをリセット",
            "btn_delete_record" to "この日記を削除",
            "current_date_log" to "指定日のアクティブログ",
            "mood_history_title" to "感情のタイムライン",
            "saved_at" to "記録時刻",
            "no_logs_on_date" to "現在、この日に記録された気分の日記がありません。上記の評価タップからスタート！✨",
            "toast_add_success" to "気分を登録しました！✨",
            "toast_update_success" to "気分の記録を更新しました！✨",
            "toast_delete_success" to "正常に削除されました",
            "choose_time" to "記録時間選択",

            "cal_heatmap_subtitle" to "過去 26 週間の日々の感情ヒートマップ",
            "cal_legend_less" to "低い評価",
            "cal_legend_more" to "高い評価",
            "cal_detail_title" to "日記の詳細",
            "streak_display" to "現在 %d 日連続の記録です！🔥",

            "ins_all_time_title" to "最近7日間の感情推移",
            "ins_distribution_title" to "感情別の割合",
            "ins_top_activities" to "よく行う活動の統計",
            "ins_card_empty" to "十分なデータがありません。まずは数日間日記を書き続けてみましょう！✨",
            
            "backup_title" to "データのバックアップ＆復元",
            "backup_desc" to "感情データをローカルの JSON 形式ファイルとしてエクスポートしたり、バックアップファイルからインポートして元に戻すことができます。すべてのデータはデバイス内部に完全保存されます。",
            "btn_export" to "バックアップ出力",
            "btn_import" to "読み込んで復元",
            "toast_export_success" to "正常にバックアップを出力しました！",
            "toast_import_success" to "バックアップのインポート・復元が完了しました！",
            "toast_import_fail" to "復元に失敗しました。JSON構造が不適切です",
            "export_filename_hint" to "心情日記_データのバックアップ",

            "tag_manager_title" to "活動アイコン・カテゴリ編集",
            "tag_manager_desc" to "既存のアクティビティをタップして名前や絵文字アイコンを編集・削除できます。絵文字(Emoji)の入力を推奨しています。",
            "tag_panel_edit" to "✏️ 選択した活動を変更",
            "tag_panel_add" to "➕ 新しいアクティビティを追加",
            "tag_label_emoji" to "絵文字",
            "tag_label_name" to "名前",
            "tag_placeholder_emoji" to "🍲",
            "tag_placeholder_name" to "例: サイクリング",
            "btn_tag_delete" to "🗑️ 完全に削除",
            "btn_tag_cancel" to "キャンセル",
            "btn_tag_save" to "変更を適用",
            "btn_tag_add" to "作成して保存",
            "toast_tag_delete_success" to "活動の削除完了: %s",
            "toast_tag_update_success" to "アクティビティを更新しました！✨",
            "toast_tag_add_success" to "アクティビティを追加しました！✨",
            "toast_tag_error" to "無効。名前の重複、または名前の未入力",

            "theme_title" to "テーマデザインの設定",
            "theme_mode_auto" to "システムの自動連動 (Auto)",
            "theme_mode_light" to "爽やかなミントカラー (Light)",
            "theme_mode_dark" to "大人のオブシディアン (Dark)",

            "lang_settings_title" to "言語切り替え設定",
            "lang_auto" to "システムの言語 (Default)",
            "lang_zh_cn" to "简体中文 (ZH)",
            "lang_zh_tw" to "繁體中文 (ZH-TW)",
            "lang_en" to "English (EN)",
            "lang_ko" to "한국어 (KO)",
            "lang_ja" to "日本語 (JA)",

            "advice_excellent" to "最高の一日。ハッピーな気持ちをキープ！",
            "advice_good" to "今日も素敵な一日ですね～",
            "advice_neutral" to "平凡な心地よさに感謝を。",
            "advice_bad" to "気分が少し浮き沈みしたり疲れているかも...",
            "advice_terrible" to "自分を大切にして穏やかな時間を過ごしましょう...",

            "ins_total_records" to "総ログ日記数",
            "ins_average_rating" to "平均感情評価",
            "ins_happy_rate" to "ハッピー率"
        )
    )

    // Translate a key
    fun getText(lang: String, key: String): String {
        val dict = translations[lang] ?: translations[LANG_EN] ?: emptyMap()
        return dict[key] ?: translations[LANG_EN]?.get(key) ?: key
    }

    // Dynamic translate the 10 core pre-populated default tag names
    private val tagTranslations = mapOf(
        "运动" to mapOf(LANG_ZH_CN to "运动", LANG_ZH_TW to "運動", LANG_EN to "Sports", LANG_KO to "운동", LANG_JA to "運動"),
        "美食" to mapOf(LANG_ZH_CN to "美食", LANG_ZH_TW to "美食", LANG_EN to "Food", LANG_KO to "음식", LANG_JA to "グルメ"),
        "娱乐" to mapOf(LANG_ZH_CN to "娱乐", LANG_ZH_TW to "娛樂", LANG_EN to "Gaming", LANG_KO to "엔터테인먼트", LANG_JA to "エンタメ"),
        "社交" to mapOf(LANG_ZH_CN to "社交", LANG_ZH_TW to "社交", LANG_EN to "Social", LANG_KO to "사교", LANG_JA to "交流"),
        "工作" to mapOf(LANG_ZH_CN to "工作", LANG_ZH_TW to "工作", LANG_EN to "Work", LANG_KO to "업무", LANG_JA to "仕事"),
        "学习" to mapOf(LANG_ZH_CN to "学习", LANG_ZH_TW to "學習", LANG_EN to "Study", LANG_KO to "공부", LANG_JA to "勉強"),
        "睡眠" to mapOf(LANG_ZH_CN to "睡眠", LANG_ZH_TW to "睡眠", LANG_EN to "Sleep", LANG_KO to "수면", LANG_JA to "睡眠"),
        "家务" to mapOf(LANG_ZH_CN to "家务", LANG_ZH_TW to "家務", LANG_EN to "Chores", LANG_KO to "집안일", LANG_JA to "家事"),
        "购物" to mapOf(LANG_ZH_CN to "购物", LANG_ZH_TW to "購物", LANG_EN to "Shopping", LANG_KO to "쇼핑", LANG_JA to "買い物"),
        "户外" to mapOf(LANG_ZH_CN to "户外", LANG_ZH_TW to "戶外", LANG_EN to "Outdoor", LANG_KO to "야외", LANG_JA to "アウトドア")
    )

    fun getTranslatedTagName(lang: String, originalName: String): String {
        return tagTranslations[originalName]?.get(lang) ?: originalName
    }
}
