import os
import re

def process_file(filepath, is_entity=False):
    with open(filepath, 'r') as f:
        content = f.read()

    # Find where the class starts
    class_match = re.search(r'(public class [^{]+\{)', content)
    if not class_match: return
    
    class_start = class_match.start()
    before_class = content[:class_start]
    after_class_start = content[class_start:]
    
    # Imports
    if is_entity:
        imports = "\nimport lombok.Getter;\nimport lombok.Setter;\nimport lombok.NoArgsConstructor;\n"
        annotations = "@Getter\n@Setter\n@NoArgsConstructor\n"
    else:
        imports = "\nimport lombok.Data;\nimport lombok.NoArgsConstructor;\nimport lombok.AllArgsConstructor;\n"
        annotations = "@Data\n@NoArgsConstructor\n@AllArgsConstructor\n"
        
    if "import lombok." not in before_class:
        # Insert import after the package declaration
        pkg_match = re.search(r'package .+;\n', before_class)
        if pkg_match:
            before_class = before_class[:pkg_match.end()] + imports + before_class[pkg_match.end():]
            
    # Remove getters and setters and constructors
    # This regex is a simple heuristic: remove public methods that look like getters/setters/constructors
    
    lines = after_class_start.split('\n')
    new_lines = []
    skip = False
    brace_count = 0
    
    for line in lines:
        stripped = line.strip()
        
        # Detect start of method
        if re.match(r'^public\s+[\w<>]+\s+\w+\s*\(.*\)\s*\{', stripped) or re.match(r'^public\s+\w+\s*\(.*\)\s*\{', stripped):
            skip = True
            brace_count = line.count('{') - line.count('}')
            continue
            
        if skip:
            brace_count += line.count('{') - line.count('}')
            if brace_count <= 0:
                skip = False
            continue
            
        new_lines.append(line)

    new_after_class = '\n'.join(new_lines)
    
    # Add annotations before class
    class_decl_match = re.search(r'public class', new_after_class)
    if class_decl_match:
        final_content = before_class + annotations + new_after_class
    else:
        final_content = before_class + new_after_class
        
    with open(filepath, 'w') as f:
        f.write(final_content)

for root, _, files in os.walk('backend/server/src/main/java/com/null0xff/ark/server/entity'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file), True)
            
for root, _, files in os.walk('backend/server/src/main/java/com/null0xff/ark/server/dto'):
    for file in files:
        if file.endswith('.java'):
            process_file(os.path.join(root, file), False)
