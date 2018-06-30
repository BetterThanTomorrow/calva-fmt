import * as config from './config';
const { formatText, cljify, jsify } = require('../lib/calva_fmt');

export function format(text: string) {
    let d = { "text": text, "config": config.getConfig() };
    d = formatText(cljify(d));
    d = jsify(d);
    if (!d["error"]) {
        return d["text"];
    }
    else {
        console.log(d["error"]);
        return text;
    }
}
