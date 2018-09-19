require 'rubygems'
require 'mechanize'

client = Mechanize.new
ARGV.each do |name|
    titles = []
    file = File.open name, "rb"
    html = client.html_parser.parse file.read, nil, "WINDOWS-1251"
    title_rows = html.xpath "/html/body/table[last()]/tr[last()]/td/table[last()]/tr/td[1]/table/tr[count(td) = 3]"
    title_rows.each do |row|
        titles.push row.xpath("td[2]").text.strip
    end
    file.close
    out = File.open name + "titles", "wb"
    out.puts titles.join "\t"
    out.close
end



