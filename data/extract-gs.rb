require 'rubygems'
require 'mechanize'

client = WWW::Mechanize.new
File.new(ARGV[0]).each_line do |line|
    page = client.get line.sub("_", "-")
    options = page.parser.xpath "//select[@name = 'gs']/option"
    options.each do |option|
        url = option.attributes["value"]
        puts url.text if url
    end
    
    STDOUT.flush
    sleep 5
end



