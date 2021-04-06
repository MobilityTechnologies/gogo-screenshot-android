# Ignore inline messages which lay outside a diff's range of PR
github.dismiss_out_of_range_messages({
    error: false, # Show all Error
    warning: true,
    message: true,
    markdown: true
})

# Create inline comments to report warning or more serious issues which happen only on modified files
android_lint.tap do |plugin|
  plugin.skip_gradle_task = true
  plugin.filtering = true
  plugin.severity = 'Warning'

  Dir.glob("**/lint-results*.xml").each do |xml|
    plugin.report_file = xml
    plugin.lint(inline_mode: true)
  end
end

## Create inline comments to report checkstyle issues which happen only on modified files
#checkstyle_reports.tap do |plugin|
#  plugin.inline_comment=true
#
#  # Report lint warnings
#  Dir.glob("**/checkstyle.xml").each do |xml|
#    plugin.report(xml, modified_files_only: true)
#  end
#end

# ktlint checkstyle
checkstyle_format.tap do |plugin|
    plugin.base_path = Dir.pwd
    Dir.glob("**/ktlint/*/ktlint*.xml").each do |xml|
      checkstyle_format.report xml
    end
end