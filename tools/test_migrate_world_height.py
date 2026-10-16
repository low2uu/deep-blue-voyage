import importlib.util
from pathlib import Path
import unittest

spec = importlib.util.spec_from_file_location('migration', Path(__file__).with_name('migrate-world-height.py'))
m = importlib.util.module_from_spec(spec)
spec.loader.exec_module(m)
n = m.n

class HeightMigrationTests(unittest.TestCase):
    def test_packed_boundaries(self):
        for bits in [4, 5, 9, 10, 12]:
            values = m.np.arange(4096, dtype=m.np.uint32) % (1 << bits)
            self.assertTrue(m.np.array_equal(values, m.unpack(m.pack(values, bits), bits, len(values))))

    def test_vertical_translation_crosses_sections(self):
        values = m.np.zeros(4096, dtype=m.np.uint32)
        values[15 * 256 + 7] = 1  # old Y=79, new Y=144, next section
        chunk = n.Compound({
            'xPos': n.Int(0), 'zPos': n.Int(0),
            'sections': n.List[n.Compound]([n.Compound({
                'Y': n.Byte(4), 'block_states': n.Compound({
                    'palette': n.List[n.Compound]([n.Compound({'Name': n.String(x)}) for x in ['minecraft:air', 'minecraft:oak_planks']]),
                    'data': m.pack(values, 4)})})]),
            'Heightmaps': n.Compound({'WORLD_SURFACE': m.pack([144] * 256, 9)}),
            'block_entities': n.List[n.Compound]([n.Compound({'id': n.String('minecraft:sign'), 'y': n.Int(79)})])})
        report = {'chunks': 0, 'moved_signs': 0, 'preserved_block_cells': 0}
        m.migrate_chunk(chunk, report)
        section = next(s for s in chunk['sections'] if int(s['Y']) == 9)
        bs = section['block_states']
        index = int(m.unpack(bs['data'], 4, 4096)[7])
        self.assertEqual(str(bs['palette'][index]['Name']), 'minecraft:oak_planks')
        self.assertEqual(int(chunk['block_entities'][0]['y']), 144)
        self.assertEqual(int(m.unpack(chunk['Heightmaps']['WORLD_SURFACE'], 10, 256)[0]), 401)
        self.assertEqual(int(chunk['sections'][0]['Y']), -16)

    def test_virtual_ship_coordinates_are_not_lifted(self):
        entity = n.Compound({'Pos': n.List[n.Double]([20481032., 132., 20481032.])})
        m.shift_entity(entity)
        self.assertEqual(float(entity['Pos'][1]), 132.)
        player = n.Compound({'Pos': n.List[n.Double]([60., 69., 150.])})
        m.shift_player(player)
        self.assertEqual(float(player['Pos'][1]), 134.)

    def test_sable_relative_section_indices_preserve_absolute_y(self):
        chunk=n.Compound({'sections': n.Compound({'12':n.Compound()}),
                          'heightmaps': n.Compound({'OCEAN_FLOOR':m.pack([200]*256,9)}),
                          'block_entities':n.List[n.Compound]([n.Compound({'y':n.Int(132)})])})
        plot=n.Compound({'chunks':n.Compound({'0':chunk})})
        m.migrate_ship_plot(plot)
        self.assertIn('24',chunk['sections'])
        self.assertEqual(-256+24*16,-64+12*16)
        self.assertEqual(int(chunk['block_entities'][0]['y']),132)
        self.assertEqual(int(m.unpack(chunk['heightmaps']['OCEAN_FLOOR'],10,256)[0]),392)

if __name__ == '__main__':
    unittest.main()
