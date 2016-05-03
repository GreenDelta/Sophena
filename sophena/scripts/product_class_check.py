'''
Generates basic class information for the product data.
requires pyyaml and pystache to be installed:
   pip install pyyaml & pip install pystache
'''

import yaml
import pystache


# see https://mustache.github.io/mustache.5.html for the
# template syntax
TEMPLATE = '''package sophena.model;

public class {{jType}} {
    
{{#fields}}    
    public {{dataType}} {{jField}};  

{{/fields}}    
}

'''


def main():
    with open("product_spec.yaml", 'r') as stream:
        model = yaml.load(stream)
        for product in model:
            pystache.render(TEMPLATE, product)
            print(pystache.render(TEMPLATE, product))

if __name__ == '__main__':
    main()

    